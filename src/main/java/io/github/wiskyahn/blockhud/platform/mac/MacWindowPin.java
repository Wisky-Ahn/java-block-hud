package io.github.wiskyahn.blockhud.platform.mac;

import com.sun.jna.Function;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import io.github.wiskyahn.blockhud.platform.WindowPinService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * macOS 바탕화면 레벨 고정. Objective-C 런타임(objc_msgSend)으로 NSApplication의 창들을 순회해
 * 제목이 일치하는 NSWindow의 level을 {@code kCGDesktopWindowLevel}로 설정한다.
 *
 * <p>모든 호출은 best-effort이며 실패해도 앱 동작에 영향 없음(상위에서 toBack 폴백).
 */
public final class MacWindowPin implements WindowPinService {

    private static final Logger log = LoggerFactory.getLogger(MacWindowPin.class);
    private static final int kCGDesktopWindowLevelKey = 2;

    private final NativeLibrary objc = NativeLibrary.getInstance("objc");
    private final NativeLibrary coreGraphics =
            NativeLibrary.getInstance("CoreGraphics");
    private final Function msgSend = objc.getFunction("objc_msgSend");

    @Override
    public void pinToDesktop(String titleMarker) {
        try {
            int desktopLevel = coreGraphics.getFunction("CGWindowLevelForKey")
                    .invokeInt(new Object[]{kCGDesktopWindowLevelKey});

            Pointer app = sendP(cls("NSApplication"), sel("sharedApplication"));
            Pointer windows = sendP(app, sel("windows"));
            long count = msgSend.invokeLong(new Object[]{windows, sel("count")});

            Pointer objectAtIndex = sel("objectAtIndex:");
            Pointer titleSel = sel("title");
            Pointer utf8Sel = sel("UTF8String");
            Pointer setLevel = sel("setLevel:");

            int pinned = 0;
            for (long i = 0; i < count; i++) {
                Pointer window = msgSend.invokePointer(new Object[]{windows, objectAtIndex, i});
                if (window == null) {
                    continue;
                }
                Pointer title = msgSend.invokePointer(new Object[]{window, titleSel});
                String text = title == null ? "" : utf8(title, utf8Sel);
                if (text.contains(titleMarker)) {
                    msgSend.invokeVoid(new Object[]{window, setLevel, (long) desktopLevel});
                    pinned++;
                }
            }
            log.info("바탕화면 레벨 고정: {}개 창 (level={})", pinned, desktopLevel);
        } catch (Throwable t) {
            log.warn("macOS 창 고정 실패(폴백 toBack 사용): {}", t.getMessage());
        }
    }

    private String utf8(Pointer nsString, Pointer utf8Sel) {
        Pointer cstr = msgSend.invokePointer(new Object[]{nsString, utf8Sel});
        return cstr == null ? "" : cstr.getString(0);
    }

    private Pointer cls(String name) {
        return objc.getFunction("objc_getClass").invokePointer(new Object[]{name});
    }

    private Pointer sel(String name) {
        return objc.getFunction("sel_registerName").invokePointer(new Object[]{name});
    }

    private Pointer sendP(Pointer receiver, Pointer selector) {
        return msgSend.invokePointer(new Object[]{receiver, selector});
    }
}
