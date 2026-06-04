# Java Block HUD — 설계 문서

> 원본 [DMeloper's Block HUD](https://github.com/d-meloper/dmelopers-block-hud) (Windows Rainmeter 스킨)을
> **크로스플랫폼 Java 데스크톱 HUD 앱**으로 재구현한다.

---

## 1. 목표와 범위

- **범위**: 전체 기능 1:1 복제 (핫바·인벤토리·에디터·인디케이터·시계·설정·버전관리/업데이터·구버전 import·진단)
- **플랫폼**: 크로스플랫폼 (Windows / macOS / Linux)
- **GUI**: JavaFX

### "크로스플랫폼 1:1"의 의미
원본은 Windows 셸 명령(`explorer.exe shell:RecycleBinFolder`, `shell:::{GUID}`)에 의존한다.
이를 **기능적 1:1**로 옮긴다 — "휴지통 열기"·"내 PC 열기" 같은 **well-known 액션**을 정의하고
각 OS 구현체가 처리한다. 사용자가 직접 지정한 절대경로(`C:\foo.exe`)는 해당 OS에서만 동작(원본도 동일).

### 플랫폼 지원 매트릭스 (정직한 점검)

| 기능 | Win | macOS | Linux | 비고 |
|------|:---:|:---:|:---:|------|
| 투명·항상위 오버레이 | ✅ | ✅ | ⚠️ | Linux는 **컴포지터 필요**(현대 DE 기본 탑재, 일부 경량 WM 제외) |
| 슬롯/폰트 렌더링 | ✅ | ✅ | ✅ | JavaFX 표준 |
| 프로그램·URL·경로 실행 | ✅ | ✅ | ✅ | `Desktop` / `ProcessBuilder` |
| well-known 액션(내PC·휴지통) | ✅ | ✅ | ✅ | Finder / `~/.Trash` / `trash://` 매핑 |
| 파일·이미지 선택창 | ✅ | ✅ | ✅ | JavaFX `FileChooser` |
| CPU·RAM 지표 | ✅ | ✅ | ✅ | OSHI |
| **GPU·VRAM 실시간 사용률** | ⚠️ | ⚠️ | ⚠️ | 최대 난점. OSHI는 VRAM 용량·모델만 안정 제공. 실시간 사용률은 OS·드라이버별 제한 → **지원 시 표시, 미지원 시 graceful 숨김** |
| 시작프로그램 등록 | ✅ | ✅ | ✅ | 레지스트리 / LaunchAgents plist / `~/.config/autostart/*.desktop` |
| 드래그·스냅 | ✅ | ✅ | ✅ | 멀티모니터 좌표 처리 유의 |
| 업데이터(GitHub) | ✅ | ✅ | ✅ | HttpClient |
| 설치 패키지 빌드 | ✅ | ✅ | ✅ | jpackage **크로스빌드 불가** → CI 3-OS 매트릭스(.msi/.dmg/.deb) |

**설계 원칙**: 플랫폼 의존 기능은 전부 `platform.PlatformShell` 등 인터페이스 뒤로 숨기고,
미지원 기능은 예외가 아니라 **graceful degradation**(숨김/비활성)으로 처리한다.

---

## 2. 기술 스택

| 영역 | 선택 | 비고 |
|------|------|------|
| 언어 | **Java 21 (LTS)** | record / sealed / pattern matching 활용 |
| 빌드 | **Gradle (Kotlin DSL)** | 크로스플랫폼 패키징 유리 |
| GUI | **JavaFX 21** | 투명·항상위 오버레이, Canvas 렌더링, CSS 스타일 |
| 시스템 지표 | **OSHI (oshi-core)** | CPU/RAM/GPU/VRAM 크로스플랫폼 통일 → 20개 PS1 중 LoadComputerInfo 대체 |
| JSON | **Jackson** | UTF-16 `.inc` → JSON 영속화 |
| 로깅 | **SLF4J + Logback** | Diagnostics.lua / RainmeterLogMonitor 대체 |
| i18n | **ResourceBundle (.properties)** | ko-KR / en-US |
| HTTP | **java.net.http.HttpClient** | GitHub Releases 업데이터 |
| 패키징 | **jpackage + jlink** | OS별 네이티브 설치본 (.msi/.dmg/.deb) |
| 테스트 | **JUnit 5 + TestFX** | 도메인/서비스 + UI |

---

## 3. 아키텍처 (계층형 / MVVM)

원본의 좋은 분리(repository / service / validation / render / state)를 그대로 계승한다.

```
┌─────────────────────────────────────────────────────┐
│  ui (JavaFX, MVVM)   hud · settings · editor · modal  │  ← .ini 메터 대체
├─────────────────────────────────────────────────────┤
│  service   EditorItemService · Validation · Launcher  │  ← .lua 로직 대체
│            SystemMetrics · IndicatorSourceResolver    │
│            LayoutEngine · ZOrderService · Localization │
│            UpdateService · UndoRedo · StartupService  │
├─────────────────────────────────────────────────────┤
│  domain    Item · Slot · ItemAction · Indicator · Theme│  ← 순수 모델
├─────────────────────────────────────────────────────┤
│  data      ItemRepository · SettingsRepository        │  ← .inc 데이터 대체
│            ImageAdjustmentRepo · LegacyImporter       │
├─────────────────────────────────────────────────────┤
│  platform  PlatformShell(Win/Mac/Linux) · FilePicker  │  ← 20개 .ps1 대체
│            StartupRegistrar · MinecraftSkinFetcher    │
└─────────────────────────────────────────────────────┘
```

### 패키지 구조
```
io.github.wiskyahn.blockhud
├── app          # 진입점, DI 와이어링, AppPaths, AppConfig
├── domain
│   ├── model    # Item, Slot, SlotGrid, Theme, LayoutState, IndicatorReading
│   └── action   # ItemAction(sealed): LaunchProgram·OpenPath·OpenUrl·Internal
├── data
│   ├── repository  # ItemRepository, SettingsRepository, ImageAdjustmentRepository
│   ├── dto         # Jackson 직렬화 DTO
│   └── legacy      # LegacyImporter (구버전 UTF-16 .inc → JSON)
├── service      # 위 service 계층 전부
├── platform
│   ├── windows  # WindowsShell, WindowsStartupRegistrar
│   ├── mac      # MacShell, MacStartupRegistrar
│   └── linux    # LinuxShell, LinuxStartupRegistrar
├── ui
│   ├── hud      # HotbarWindow, InventoryWindow, IndicatorWindow, ClockWindow
│   ├── settings # SettingsWindow + ViewModel
│   ├── editor   # EditorWindow + ViewModel
│   ├── modal    # ModalWindow, TooltipPopup
│   └── common   # SlotGridView, IndicatorBar, PixelFont, DragSnapSupport, theme CSS
└── i18n         # LocalizationService + messages_ko.properties / messages_en.properties
```

---

## 4. 도메인 모델 (원본 데이터 매핑)

원본 슬롯 데이터: `HotbarItem_Slot01_Label / _Action / _Image / _Qty` + 이미지 정렬 오프셋.

```java
public record Item(
    int slot,
    String label,        // 원본 Label / ItemName
    ItemAction action,   // 원본 Action / ExecPath
    String image,        // 원본 Image (파일명)
    int qty,             // 원본 Qty
    ImageOffset offset,  // OffsetX / OffsetY / SizeOffset
    boolean confirmBeforeRun
) {}

public sealed interface ItemAction
    permits LaunchProgram, OpenPath, OpenUrl, WellKnownTarget, InternalCommand {}
// 문자열 파싱 규칙:
//   http(s)://...                 → OpenUrl
//   _OPEN_INVENTORY_ 등            → InternalCommand
//   explorer.exe shell:... / {GUID}→ WellKnownTarget (OS별 매핑)
//   그 외                          → LaunchProgram (절대경로/실행파일)
```

### JSON 영속화 예시 (`items.json`)
```json
{
  "hotbar": [
    { "slot": 1, "label": "내 컴퓨터", "image": "Computer.png",
      "action": { "type": "wellKnown", "target": "THIS_PC" },
      "qty": 0, "offset": { "x": 0, "y": 0, "size": 0 } }
  ],
  "inventory": [ /* ... */ ]
}
```
저장 위치: `AppPaths.configDir()`
- Windows: `%APPDATA%\DMeloperBlockHud`
- macOS: `~/Library/Application Support/DMeloperBlockHud`
- Linux: `~/.config/dmeloper-block-hud`

---

## 5. 핵심 기술 과제와 해법

| 과제 | 원본 | Java 해법 |
|------|------|----------|
| 투명·항상위 오버레이 | Rainmeter 엔진 | `StageStyle.TRANSPARENT` + `setAlwaysOnTop(true)` + `Scene fill=TRANSPARENT` |
| 슬롯 그리드 렌더링 | meter(ImageContainer) | `SlotGridView`: hotbar.png 위 ImageView 레이어 + 호버 하이라이트 |
| 픽셀 폰트 | Galmuri .ttf | `Font.loadFont(...)` + CSS |
| 시스템 지표→마크 바 | PS1 + measure | OSHI 폴링 → `IndicatorBar` (10칸 풀/하프/엠티 아이콘). 소스 12종(§8.2) |
| 반응형 레이아웃 | ResponsiveLayoutCore.lua | `LayoutEngine`: anchor+reference 의존그래프 + 균등 스케일(§8.1) |
| 창 쌓임 순서(Z) | ZPosArrangement.lua | `ZOrderService`: 스킨 간 항상위 레이어 정렬 |
| 프로그램/URL 실행 | Rainmeter Bang | `PlatformShell` (ProcessBuilder / Desktop.browse) |
| 파일·이미지 선택창 | PickPath.ps1 / PickImage.ps1 | JavaFX `FileChooser` / `DirectoryChooser` |
| 드래그 & 스냅 | `!Draggable` / `!SnapEdges` | 마우스 드래그 핸들러 + 화면 엣지 스냅 계산 |
| 시작프로그램 등록 | StartupAutoRun.ps1 | `StartupRegistrar` (레지스트리 / LaunchAgents / .desktop) |
| 버전관리·업데이트 | 다수 PS1 | `UpdateService` (GitHub Releases API + HttpClient) |
| 구버전 import | ImportFromOldVersion.ps1 | `LegacyImporter`: UTF-16 `.inc` 파서 → JSON |
| 다국어 | Localization.lua + .inc | `ResourceBundle` |
| 실행취소/재실행 | SettingsState.lua | `UndoRedoService` (command 스택) |

---

## 6. 단계별 로드맵

| Phase | 산출물 | 핵심 검증 |
|-------|--------|-----------|
| **0. 스캐폴드** | Gradle + JavaFX + 패키지 골격 + CI | `./gradlew run` 빈 투명 창 |
| **1. 도메인·데이터** | Item/Action 모델, ItemRepository(JSON), LegacyImporter, i18n | 단위테스트: 구버전 .inc → JSON 왕복 |
| **2. 핫바·인벤토리** | SlotGridView 렌더링, ActionLauncher, 드래그/스냅 | 슬롯 클릭 → 프로그램/URL 실행 |
| **3. 에디터** | EditorWindow, 이미지 정렬, 툴팁, 모달 | 슬롯 편집 → 저장 → 반영 |
| **4. 인디케이터·시계** | OSHI 연동 IndicatorBar, ClockWindow, 스프라이트 시계 | CPU/RAM 실시간 바 표시 |
| **5. 설정창** | 테마·폰트·위치·리셋·실행취소/재실행·시작옵션 | 설정 변경 즉시 반영 |
| **6. 버전관리·진단** | UpdateService, VersionManager, 로깅/진단 | GitHub 릴리스 체크·업데이트 |
| **7. 패키징** | jpackage OS별 설치본, 반응형 레이아웃 마감 | 3개 OS 설치·실행 |

---

## 8. 정밀 설계 보강 (원본 심층 분석으로 발견)

### 8.1 반응형 레이아웃 엔진 (`LayoutEngine`)
원본 `ResponsiveLayoutCore.lua`는 단순 좌표가 아니라 **anchor + reference + offset + 균등 스케일** 시스템이다.

- **기준 해상도**: 1920×1080 (작업영역 1920×1032, 하단 예약 48px). 실제 화면 대비 **uniform scale** 적용, **0.711 ~ 1.333**로 클램프.
- **상대 배치**: 각 창은 다른 창(`reference`)의 **named anchor**에 붙는다 → 스킨 간 **의존 그래프** 형성.
- 원본 배치 정의(그대로 이식):

| 창 | reference | anchor | offset(x,y) | 의존 |
|----|-----------|--------|-------------|------|
| Hotbar | PrimaryWorkArea | BottomCenter | (-11,-39) | → 인디케이터 5종 |
| IndicatorHeart | Hotbar | HotbarVisibleLeftTop | (-1,-59) | |
| IndicatorArmor | Hotbar | HotbarVisibleLeftTop | (-1,-93) | |
| IndicatorFood | Hotbar | HotbarVisibleRightTop | (2,-59) | |
| IndicatorAir | Hotbar | HotbarVisibleRightTop | (2,-93) | |
| IndicatorExp | Hotbar | HotbarVisibleCenterTop | (1,-63) | |
| Inventory | PrimaryWorkArea | ScreenCenter | (-354,-310) | → Settings,Editor,InventoryBG,Hotbar |
| InventoryBG | PrimaryWorkArea | PrimaryWorkAreaFill | (0,0) | |
| Clock | PrimaryWorkArea | TopCenter | (13,176) | |
| ClockSprite | PrimaryWorkArea | TopCenter | (-2,62) | |
| Settings | Inventory | InventoryLeftTop | (-350,0) | → Inventory |
| Editor | Inventory | InventoryRightTop | (-4,0) | → Inventory,InventoryBG |

→ Java 구현: 창들을 의존 그래프로 **위상정렬** 후 reference→anchor 순서로 절대좌표 해석.
사용자 수동 드래그 시 해당 창은 그래프에서 분리되어 고정좌표 모드로 전환.

### 8.2 인디케이터 소스 모델 (`IndicatorSourceResolver`)
각 마크 바(하트/갑옷/배고픔/공기/경험치)는 **소스 + 레벨(최대값) + 디스크대상**을 개별 설정한다.
원본 허용 소스 **12종 + 리터럴 + disabled**:

```
cpuIdle · cpuLoad · ramFree · ramUsed · diskFree · diskUsed
batteryCharge · batteryDrain · gpuFree · gpuUsed · vramFree · vramUsed
+ 0-100 리터럴 · disabled
레벨(최대값): auto(-1) | 위 소스 | 0-9999 고정
```

→ OSHI 매핑:
- cpu/ram/disk/battery → OSHI 안정 제공 (전 OS) — **원래 GPU/VRAM만 약점, 디스크·배터리는 OK**
- gpu/vram → OSHI VRAM용량은 OK, 실시간 사용률은 §플랫폼매트릭스대로 graceful 숨김
- `disabled`/미지원 소스 → 바 숨김. 잘못된 소스 → `cpuIdle` 폴백 + 로그(원본 동작 일치)

→ `domain.IndicatorSource`(enum) + `service.IndicatorSourceResolver`로 이식. 갱신 주기 1Hz.

### 8.3 설정 시스템 (`SettingsSchema` — 데이터 드리븐)
설정창은 행을 하드코딩하지 않는다. `SettingsSchema.lua`처럼 **탭 + 필드 디스크립터**에서 폼을 생성한다.

- **7개 탭**: 기본(general) · 저사양 모드(lowSpec) · 핫바(hotbar) · 인디케이터(indicators) · 인벤토리(inventory) · 시계(clock) · UI
- **5가지 컨트롤 타입**: `toggle`(26) · `action`(16, 버튼) · `stepper`(13, 숫자증감) · `text`(13, 텍스트/색상) · `readonly`(1)
- **약 75개 키** (대표):
  - general: muteSound, language, appVersion(readonly), startupAutoRun, refreshComputerInfo, openLogFolder, openVersionManager, importLegacyData, resetAllSettings
  - **lowSpec**: freezeInventoryPlayerAnimation, disableSlotHoverHighlight, disableHoverTextTooltip ← 저사양 모드(원본에 별도 탭, 초안 누락)
  - hotbar: slotSize, itemOffset, textYOffset, textFontSize, textColor, enabled, draggable, dragSnap
  - indicators: health/armor/food/air/expSource, expLevel, expLevelGap, *DiskTarget, barScalePercent
  - inventory: itemSize, tooltipSize, bottomRow, **minecraftSkinUsername / applyMinecraftSkin / hideSteve**(마크 스킨 연동), hide{SkinFolder,Edit,Settings}Button
  - clock: clockType, spriteSize, 24Hour, hideMeridiem, timeSize, dateSize, textColor, textGap
  - ui: baseFont, settingsTheme, resetAllSkinPositions

→ Java: `record SettingField(String key, ControlType type, String labelKey, …)` 리스트 →
`SettingsFormBuilder`가 JavaFX 폼 자동 생성. `action` 타입은 핸들러에 위임(서비스 호출).

### 8.4 에디터 드래프트→커밋 모델 (`EditorDraft`)
에디터는 실데이터를 직접 고치지 않고 **드래프트 작업본**에 쓴 뒤 저장 시 커밋한다
(원본 `ItemDataRepository.IsDraftOpen` / `EditorDraft.inc` 확인).

- **슬롯 주소 체계 2종**:
  - 핫바(맨 아랫줄): 순차 `Slot01`~`Slot10`
  - 인벤토리(그리드): 좌표 `SlotX{col}Y{row}` (예: `SlotX3Y2`)
  - `useBottomSlot`/`inventoryBottomRow`: 핫바 = 인벤토리 최하단 행 → 둘 병합 로직(원본 `buildMergedInfos`)
- **흐름**: 편집 → `EditorDraft`(메모리/임시저장) → 저장 시 `items.json` 커밋 / 취소 시 폐기
- 드래프트 열림 동안 `UndoRedoService`(command 스택)가 드래프트에 작용 → 실행취소/재실행
- 필드: Label / Action / Image / Qty + 이미지 오프셋(OffsetX/Y/SizeOffset)

→ Java: `domain.SlotAddress`(sealed: HotbarSlot(n) | GridSlot(col,row)),
`service.EditorDraft`(작업본 + dirty 추적), 저장 시 `ItemRepository.commit(draft)`.

### 8.5 모달 & 툴팁 (`ModalService` / `TooltipPopup`)
- **모달**(Modal.lua): 로컬라이즈된 알림/확인 다이얼로그.
  - 메시지(플레이스홀더 치환 `{0}`) + 버튼, **콜백 이름** 기반 동작(확인 시 서비스 호출)
  - 메시지 길이에 따른 **동적 높이 계산** + 화면 중앙 정렬
  - **클립보드 복사** 지원("복사됨" 라벨 피드백)
  - → Java: `ModalService.alert(key, args)` / `confirm(key, args, onConfirm)` — JavaFX 모달 Stage,
    텍스트는 `LocalizationService` 경유. 콜백은 `Runnable`/`Consumer`로 직접 전달(이름 문자열 불필요).
- **툴팁**(Tooltip.lua): 슬롯 호버 시 아이템명/옵션 텍스트를 마우스 위치에 표시.
  - `ShowItemName` / `ShowItemNameAt(x,y)` / `Hide` / `OnMouseMove`, 픽셀 폭 측정으로 박스 크기 결정
  - 저사양 모드(`lowSpecDisableHoverTextTooltip`)면 비활성
  - → Java: `TooltipPopup`(JavaFX `Popup`), 호버 이벤트 + `Text` 노드 실측 너비.

### 8.6 버전 관리 & 업데이터 (`UpdateService` / `VersionManager`)
원본 흐름(다수 PS1): GitHub Releases 카탈로그 → variant 자산 매칭 → 다운로드 → SHA256 검증 → 교체.

- **카탈로그**: `GET api.github.com/repos/{owner}/{repo}/releases?per_page&page` (페이지네이션) →
  `tag_name` 시맨틱 버전 파싱 + 릴리스별 `assets` 목록
- **variant**: 자산명 `_Korea`/`_Global` 매칭 (원본). 크로스플랫폼 Java 앱에선 **OS별 설치본**
  (`.msi`/`.dmg`/`.deb`)이 자연스러운 variant 축이 됨 — ko/en은 이미 인앱 i18n
- **검증**: 다운로드 자산 **SHA256** 해시 대조 (`MessageDigest`)
- **설치/전환**: 압축 해제(`java.util.zip`) 후 적용. `VersionManager`로 설치본 목록·활성 버전 전환

⚠️ **크로스플랫폼 자기 업데이트 주의**: Rainmeter는 스킨 폴더 파일 교체로 끝나지만,
jpackage 네이티브 앱은 실행 중 자기 바이너리 교체가 OS별로 까다롭다. 권장 전략:
- 앱 **데이터/설정**(`AppPaths.configDir`)와 **설치 바이너리** 분리 (데이터는 그대로 유지)
- 업데이트는 "새 설치본 다운로드 → 검증 → 설치 프로그램 실행/안내" 또는 OS 패키지 매니저에 위임
- Phase 6에서 OS별 구체 전략 확정

## 9. 자산 이관

- PNG 64개 / 폰트 3개 → `src/main/resources/assets/` 로 복사 (라이선스: 폰트 Galmuri, MIT 코드/리소스 유지)
- 원본 README의 라이선스·서드파티 고지(THIRD_PARTY_NOTICES) 계승
- Mouse.dll(Windows 전용 플러그인)은 JavaFX 마우스 이벤트로 불필요 → 제거
