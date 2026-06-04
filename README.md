# Java Block HUD

마인크래프트 스타일 데스크톱 HUD — **크로스플랫폼(Windows · macOS · Linux) Java 앱**.

Windows 전용 Rainmeter 스킨 [DMeloper's Block HUD](https://github.com/d-meloper/dmelopers-block-hud)를
별도 프로그램 없이 단독 실행되는 **JavaFX 데스크톱 앱**으로 재구현하는 프로젝트입니다.

> A cross-platform Java (JavaFX) reimplementation of *DMeloper's Block HUD*,
> a Minecraft-style desktop HUD originally built as a Windows-only Rainmeter skin.

## 무엇을 하나요

바탕화면에 마인크래프트 게임 UI를 띄우고, 실제로 동작합니다.

- **핫바 / 인벤토리** — 마크 스타일 아이템 슬롯. 클릭하면 프로그램·폴더·파일·URL 실행
- **시스템 인디케이터** — 하트/갑옷/배고픔/공기/경험치 막대로 CPU·RAM·디스크·배터리·GPU 표시
- **시계** — 마크 스타일 시계 (12/24시간, 색상·크기 설정)
- **빌트인 에디터** — 슬롯 이름·실행대상·이미지·수량·정렬 편집
- **설정창** — 테마·폰트·위치·실행취소/재실행·리셋·시작프로그램·다국어(ko/en)

## 기술 스택

Java 21 · JavaFX 21 · Gradle · OSHI(시스템 지표) · Jackson(JSON) · jpackage(패키징)

자세한 아키텍처와 설계는 [DESIGN.md](DESIGN.md) 참고.

## 빌드 & 실행

JDK 21 필요. (Gradle 8.14는 JDK 25에서 실행되지 않으므로 `JAVA_HOME`을 21로 지정)

```bash
export JAVA_HOME="$(/usr/libexec/java_home -v 21)"   # macOS 예시
./gradlew run     # HUD 창 실행
./gradlew build   # 컴파일 + 테스트
```

## 상태

🚧 설계 완료 / 구현 시작 단계 (Phase 0).

## 라이선스

MIT License — [LICENSE](LICENSE) 참고.
원본 *DMeloper's Block HUD* (MIT, © 2026 DMeloper) 기반의 포트입니다.

이 프로젝트는 Mojang Studios 또는 Microsoft와 무관하며, Minecraft 관련 상표는 각 소유자에게 있습니다.
