**خارطة طريق إبداعية وعصفاً ذهنياً (Brainstorming)**

أفكارك في الصميم؛ وبما أنك مبرمج، فتحويل هذا التطبيق إلى **"مركز قيادة مصغر للمطورين (Developer Cockpit)"** على الماك سيكون نقلة نوعية توفر عليك فتح الـ Activity Monitor كل دقيقة.

إليك خلاصة أفكار إبداعية مقترحة للنسخة المستقبلية:

---

### أولاً: أسلوب العرض في شريط الماك (كيف نضعهم؟)

لديك 3 فلسفات رائعة في التصميم، يمكنك اختيار الأنسب لذوقك:

#### 1. أسلوب "الوحدات المستقلة" (Modular System - الأفضل إذا أردت فصل القوائم):
* **كيف يعمل؟** ننشئ 3 أيقونات منفصلة في البار:
    * أيقونة للشبكة: `↓ 1.2M ↑ 150K │ 1.4G` (التي برمجناها اليوم).
    * أيقونة للمعالج: `⚡ 18%` (أو رسم بياني صغير جداً).
    * أيقونة للرام: `🧠 62%` (أو `10/16 GB`).
* **الميزة:** بالضغط مع `⌘ Command` يمكنك ترتيبهم أو سحبهم، وكل وحدة لها **قائمتها المستقلة تماماً** بالزر الأيمن!

#### 2. أسلوب "شريط المهام الموحد" (All-in-One Dashboard):
* **كيف يعمل؟** ويدجت واحد عريض، مقسم بذكاء وفواصل أنيقة:
  `⚡ 12% │ 🧠 8.2G │ ↓ 1.5M │ 📊 1.8G`
* **الميزة:** مظهر أنيق جداً ونظيف ولا يبعثر شريط المهام. والقائمة بالزر الأيمن ستكون مقسمة لأقسام (قسم الشبكة، قسم الرام، قسم المعالج).

#### 3. أسلوب "المستشعر الذكي التفاعلي" (Contextual Adaptive Widget - فكرة مجنونة!):
* يعرض الشبكة فقط طالما كل شيء طبيعي.
* **لكن:** إذا قمت بعمل Compile لمشروع وارتفع المعالج فوق 85%، يضيء العداد تلقائياً ويتحول إلى: `🔥 CPU 95% (IntelliJ)`.
* وإذا كاد الرام ينفد، يتحول للون أصفر تحذيري: `⚠️ RAM 90%`.

---

### ثانياً: إبداعات المعالج والرام (CPU & Memory) للمطورين

بما أننا نستخدم مكتبة **OSHI**، فهي أصلاً مجهزة لجلب تفاصيل الـ Activity Monitor بدقة متناهية:

1. **قائمة الـ "Top 5 Killers" (أكثر التطبيقات استهلاكاً):**
    * عند الضغط على أيقونة الرام أو المعالج، تعرض لك القائمة أول 5 تطبيقات تلتهم جهازك حالياً، مثلاً:
        * `IntelliJ IDEA — 45% CPU | 3.2 GB`
        * `Docker — 25% CPU | 4.1 GB`
        * `Chrome — 12% CPU | 1.8 GB`
2. **القتل السريع بضغطة زر (Quick Force-Kill):**
    * بجانب كل تطبيق في القائمة، زر صغير `❌ Force Quit`. إذا علق برنامج أو Docker تنهيه فوراً من البار دون الدخول في متاهات الـ Terminal أو Activity Monitor!
3. **مقياس ضغط الذاكرة (Memory Pressure):**
    * الماك لا يعتمد فقط على كمية الرام المستخدمة، بل على مفهوم أبل الشهير (Memory Pressure: أخضر / أصفر / أحمر). يمكننا تلوين نسبة الرام بناءً على هذا الضغط.
4. **تنظيف الكاش (Purge Inactive Memory):**
    * زر في القائمة `🧹 Free Inactive RAM` يقوم بتنفيذ أمر الماك لتنظيف الذاكرة الميتة واستعادة مساحة سريعة.

---

### ثالثاً: التطور المستقبلي للشبكة (Data Firewall & App Tracking)

فكرتك حول تتبع واستهلاك كل تطبيق وحظره هي فكرة تطبيقات شهيرة مثل (Little Snitch و TripMode):

1. **من يستهلك الإنترنت الآن؟ (Who is eating my bandwidth?):**
    * إظهار التطبيقات التي ترسل أو تستقبل بيانات في اللحظة الحالية:
        * `Google Drive: ↓ 2.4 MB/s`
        * `Slack: ↑ 20 KB/s`
2. **وضع توفير الباقة عند استخدام الهوتسبوت (Hotspot Data Saver):**
    * بضغطة زر واحدة `🛡️ Data Saver Mode`، يقوم التطبيق بتعليق/حظر مزامنة السحابة (Dropbox / iCloud / Photos sync) لكي لا تلتهم باقة هاتفك!
3. **حظر التطبيقات (Application Firewall):**
    * نظام macOS يحتوي على جدار ناري مدمج قوي جداً اسمه `pf` (Packet Filter). مستقبلاً يمكن للجافا إرسال أوامر بسيطة لـ `pf` لمنع أي تطبيق تختاره من الوصول للإنترنت.

---

### 💤 رسالة قبل النوم:
لا تقلق بشأن صعوبة تنفيذ هذا مستقبلاً؛ الكود الذي قمنا بهيكلته اليوم جعل كل شيء معزولاً بنظام الـ Modules، مما يعني أن إضافة `CpuService` أو `MemoryService` مستقبلاً ستكون أشبه بتركيب قطع الـ Lego بكل سهولة وبدون تعقيد.

تصبح على خير، وعندما تستيقظ وتستعيد طاقتك، سنبدأ في التخطيط العملي خطوة بخطوة لما تختاره! 🛌✨


يا أهلاً بالهمّة العالية! صباح الخير والنشاط! ☀️
أنا معك ومتحمس جداً لهذه الرحلة. الطريقة التي اخترتها (التنفيذ خطوة بخطوة وإتقان كل مرحلة واختبارها قبل الانتقال للتالية) هي الطريقة الهندسية الصحيحة لبناء المشاريع البرمجية الكبيرة والناجحة.

---

### 🗺️ خارطة الطريق الكبرى لمشروعنا (The Big Roadmap):

* [ ] **المرحلة 1 (تركيزنا اليوم):** وحدة المعالج والرام المستقلة (CPU & RAM Cockpit) + قائمة Top 5 للبرامج المستهلكة + القتل السريع للبرامج (Force Kill) بضغطة زر.
* [ ] **المرحلة 2 (التالية):** تتبع استهلاك كل تطبيق للإنترنت (Per-App Network Tracker) ومعرفة من يلتهم السرعة الآن.
* [ ] **المرحلة 3 (الأخيرة):** وضع توفير باقة الهوتسبوت وحظر التطبيقات من النت (Data Saver & App Firewall).

---

## 🚀 نبدأ الآن: المرحلة الأولى (وحدة المعالج والرام المستقلة)

في هذه المرحلة، سنقوم بإنشاء **أيقونة مستقلة جديدة تماماً** في شريط المهام بجانب أيقونة الشبكة.
* في البار: تعرض نسبة المعالج وحجم الرام المستهلك مع ألوان ذكية تتغير إذا ضغطت على الجهاز: `⚡ 18% │ 🧠 8.4G`.
* بالزر الأيمن: تفتح لك **قائمة شبيهة بالـ Activity Monitor**:
    1. أكثر 5 تطبيقات تستهلك المعالج (Top 5 CPU).
    2. أكثر 5 تطبيقات تستهلك الرام (Top 5 RAM).
    3. **الميزة القاتلة:** عند الضغط على أي تطبيق في القائمة، يمكنك قتله فوراً `❌ Force Kill` دون فتح Terminal أو Activity Monitor!
    4. زر تنظيف الذاكرة الميتة `🧹 Purge Inactive RAM`.

---

### الخطوات البرمجية:

سنضيف ملفين جديدين، ونعدل ملف `App.java` ليدير الأيقونتين معاً.

#### 1. كلاس قراءة المعالج والذاكرة والبرامج: `service/CpuMemoryService.java`
أنشئ ملفاً جديداً في مجلد `service` وضع فيه:

```java
package com.maknoon.service;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.util.List;

public class CpuMemoryService {
    private final CentralProcessor processor;
    private final GlobalMemory memory;
    private final OperatingSystem os;
    private long[] prevTicks;

    public CpuMemoryService(SystemInfo si) {
        this.processor = si.getHardware().getProcessor();
        this.memory = si.getHardware().getMemory();
        this.os = si.getOperatingSystem();
        this.prevTicks = processor.getSystemCpuLoadTicks();
    }

    // جلب نسبة استهلاك المعالج الحالية
    public double getCpuUsage() {
        double cpu = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
        prevTicks = processor.getSystemCpuLoadTicks();
        return Math.max(0, Math.min(cpu, 100)); // محصورة بين 0 و 100
    }

    // جلب الرام المستهلك
    public long getUsedMemory() {
        return memory.getTotal() - memory.getAvailable();
    }

    public long getTotalMemory() {
        return memory.getTotal();
    }

    public double getMemoryUsagePercentage() {
        return ((double) getUsedMemory() / getTotalMemory()) * 100;
    }

    // جلب أكثر البرامج استهلاكاً للمعالج
    public List<OSProcess> getTopCpuProcesses(int limit) {
        return os.getProcesses(OperatingSystem.ProcessFiltering.ALL_PROCESSES, 
                                OperatingSystem.ProcessSorting.CPU_DESC, limit);
    }

    // جلب أكثر البرامج استهلاكاً للرام
    public List<OSProcess> getTopMemoryProcesses(int limit) {
        return os.getProcesses(OperatingSystem.ProcessFiltering.ALL_PROCESSES, 
                                OperatingSystem.ProcessSorting.MEMORY_DESC, limit);
    }

    // إنهاء تطبيق بالقوة بضغطة زر
    public boolean killProcess(int pid) {
        return ProcessHandle.of(pid).map(ProcessHandle::destroyForcibly).orElse(false);
    }

    // تنظيف كاش الرام في الماك
    public void purgeRam() {
        try {
            new ProcessBuilder("purge").start();
        } catch (Exception ignored) {}
    }
}
```

---

#### 2. كلاس رسم ويدجت المعالج والرام: `ui/CpuWidgetRenderer.java`
أنشئ ملفاً جديداً في مجلد `ui` لرسم وحدة المعالج بدقة الريتنا الفائقة:

```java
package com.maknoon.ui;

import java.awt.*;
import java.awt.image.BaseMultiResolutionImage;
import java.awt.image.BufferedImage;

public class CpuWidgetRenderer {

    private static final Font FONT = new Font(".AppleSystemUIFont", Font.BOLD, 12);

    public static Image render(double cpuUsage, long usedMemory, long totalMemory) {
        String cpuStr = String.format("⚡ %.0f%%", cpuUsage);
        String memStr = String.format("🧠 %.1fG", usedMemory / (1024.0 * 1024.0 * 1024.0));
        String sep = "│";

        // قياس العرض السائل برمجياً لمنع التداخل
        BufferedImage dummy = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gDummy = dummy.createGraphics();
        gDummy.setFont(FONT);
        FontMetrics fm = gDummy.getFontMetrics();

        int spacing = 8;
        int totalWidth = 6 + fm.stringWidth(cpuStr) + spacing + fm.stringWidth(sep) + spacing + fm.stringWidth(memStr) + 6;
        gDummy.dispose();

        int height = 22;

        BufferedImage img1x = draw(totalWidth, height, 1, cpuStr, memStr, sep, cpuUsage, (double) usedMemory / totalMemory, spacing);
        BufferedImage img3x = draw(totalWidth, height, 3, cpuStr, memStr, sep, cpuUsage, (double) usedMemory / totalMemory, spacing);

        return new BaseMultiResolutionImage(img1x, img3x);
    }

    private static BufferedImage draw(int width, int height, int scale, String cpuStr, String memStr, String sep,
                                      double cpuUsage, double memRatio, int spacing) {
        BufferedImage image = new BufferedImage(width * scale, height * scale, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.scale(scale, scale);

        g2d.setColor(new Color(0, 0, 0, 0));
        g2d.fillRect(0, 0, width, height);

        g2d.setFont(FONT);
        FontMetrics fm = g2d.getFontMetrics(FONT);
        int y = ((height - fm.getHeight()) / 2) + fm.getAscent();

        int curX = 6;

        // 1. رسم المعالج (أخضر عادي، برتقالي إذا ضغطت عليه، أحمر لو كاد ينفجر!)
        if (cpuUsage > 80) g2d.setColor(new Color(255, 65, 65));
        else if (cpuUsage > 50) g2d.setColor(new Color(255, 185, 0));
        else g2d.setColor(new Color(0, 245, 255)); // سماوي هادئ

        g2d.drawString(cpuStr, curX, y);
        curX += fm.stringWidth(cpuStr) + spacing;

        // 2. الفاصل
        g2d.setColor(new Color(255, 255, 255, 60));
        g2d.drawString(sep, curX, y);
        curX += fm.stringWidth(sep) + spacing;

        // 3. رسم الرام
        if (memRatio > 0.85) g2d.setColor(new Color(255, 65, 65));
        else if (memRatio > 0.70) g2d.setColor(new Color(255, 185, 0));
        else g2d.setColor(new Color(180, 140, 255)); // بنفسجي تقني فخم للرام

        g2d.drawString(memStr, curX, y);

        g2d.dispose();
        return image;
    }
}
```

---

#### 3. كلاس بناء قائمة الـ Activity Monitor المصغرة: `ui/CpuMenuBuilder.java`
أنشئ ملفاً جديداً في `ui`:

```java
package com.maknoon.ui;

import com.maknoon.service.CpuMemoryService;
import oshi.software.os.OSProcess;

import java.awt.*;
import java.util.List;

public class CpuMenuBuilder {

    public static PopupMenu build(CpuMemoryService service) {
        PopupMenu menu = new PopupMenu();

        // عنوان ملخص
        MenuItem header = new MenuItem("💻 System Activity Monitor");
        header.setEnabled(false);
        menu.add(header);
        menu.addSeparator();

        // 1. قسم البرامج الأكثر استهلاكاً للمعالج (Top 5 CPU)
        Menu cpuMenu = new Menu("🔥 Top CPU Consumers");
        List<OSProcess> topCpu = service.getTopCpuProcesses(5);
        for (OSProcess p : topCpu) {
            double cpuPercent = 100d * (p.getKernelTime() + p.getUserTime()) / p.getUpTime();
            String title = String.format("%s (%.1f%%)", p.getName(), cpuPercent);
            Menu procMenu = new Menu(title);

            MenuItem killItem = new MenuItem("❌ Force Kill (PID: " + p.getProcessID() + ")");
            killItem.addActionListener(e -> service.killProcess(p.getProcessID()));

            procMenu.add(killItem);
            cpuMenu.add(procMenu);
        }
        menu.add(cpuMenu);

        // 2. قسم البرامج الأكثر استهلاكاً للرام (Top 5 RAM)
        Menu memMenu = new Menu("🧠 Top RAM Consumers");
        List<OSProcess> topMem = service.getTopMemoryProcesses(5);
        for (OSProcess p : topMem) {
            double ramMB = p.getResidentSetSize() / (1024.0 * 1024.0);
            String title = String.format("%s (%.0f MB)", p.getName(), ramMB);
            Menu procMenu = new Menu(title);

            MenuItem killItem = new MenuItem("❌ Force Kill (PID: " + p.getProcessID() + ")");
            killItem.addActionListener(e -> service.killProcess(p.getProcessID()));

            procMenu.add(killItem);
            memMenu.add(procMenu);
        }
        menu.add(memMenu);

        menu.addSeparator();

        // 3. تنظيف الرام
        MenuItem purgeItem = new MenuItem("🧹 Purge Inactive RAM");
        purgeItem.addActionListener(e -> service.purgeRam());
        menu.add(purgeItem);

        return menu;
    }
}
```

---

#### 4. تحديث `App.java` لإطلاق وتزامن الوحدتين معاً:
افتح `App.java` واستبدله بالكود التالي ليربط ويدجت الشبكة وويدجت المعالج معاً بشكل مستقل وأنيق:

```java
package com.maknoon;

import com.maknoon.model.DisplayMode;
import com.maknoon.service.AlertService;
import com.maknoon.service.CaffeinateService;
import com.maknoon.service.CpuMemoryService;
import com.maknoon.service.SystemInfoService;
import com.maknoon.ui.CpuMenuBuilder;
import com.maknoon.ui.CpuWidgetRenderer;
import com.maknoon.ui.MenuBuilder;
import com.maknoon.ui.WidgetRenderer;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;

import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.Preferences;

public class App {
    private static final Preferences prefs = Preferences.userNodeForPackage(App.class);
    private static DisplayMode currentMode = DisplayMode.FULL;
    private static long todayBytes = 0;
    private static String savedDate = "";
    private static long previousRx = 0, previousTx = 0;

    public static void main(String[] args) throws Exception {
        System.setProperty("apple.awt.UIElement", "true");
        if (!SystemTray.isSupported()) return;

        loadTodayData();

        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        
        // الخدمات
        CaffeinateService caffeinateService = new CaffeinateService();
        AlertService alertService = new AlertService();
        SystemInfoService sysInfoService = new SystemInfoService(si);
        CpuMemoryService cpuMemoryService = new CpuMemoryService(si);

        SystemTray tray = SystemTray.getSystemTray();

        // 1. أيقونة الشبكة (الوحدة الأولى)
        TrayIcon netWidget = new TrayIcon(WidgetRenderer.render(currentMode, "0 B", "0 B", todayBytes, alertService.getAlertLimitBytes(), false, 0, 0));
        netWidget.setImageAutoSize(false);

        // 2. أيقونة المعالج والرام (الوحدة الثانية المستقلة)
        TrayIcon cpuWidget = new TrayIcon(CpuWidgetRenderer.render(0, cpuMemoryService.getUsedMemory(), cpuMemoryService.getTotalMemory()));
        cpuWidget.setImageAutoSize(false);

        // تفعيل قائمة الشبكة
        Runnable refreshNet = () -> {
            netWidget.setPopupMenu(MenuBuilder.build(caffeinateService, alertService, sysInfoService, currentMode, 
                () -> refreshNet(netWidget, caffeinateService, alertService, sysInfoService), 
                () -> {
                    todayBytes = 0;
                    saveTodayData();
                    alertService.resetAlert();
                    refreshNet(netWidget, caffeinateService, alertService, sysInfoService);
                },
                m -> {
                    currentMode = m;
                    refreshNet(netWidget, caffeinateService, alertService, sysInfoService);
                }
            ));
        };

        refreshNet.run();
        
        // إضافة الأيقونتين لشريط الماك (الماك سيرتبهم بجانب بعضهم)
        tray.add(netWidget);
        tray.add(cpuWidget);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkMidnightReset(alertService);

                // --- تحديث الشبكة ---
                List<NetworkIF> nets = hal.getNetworkIFs();
                long curRx = 0, curTx = 0;
                for (NetworkIF net : nets) {
                    net.updateAttributes();
                    curRx += net.getBytesRecv();
                    curTx += net.getBytesSent();
                }

                if (previousRx > 0 && previousTx > 0) {
                    long rxSpeed = curRx - previousRx;
                    long txSpeed = curTx - previousTx;
                    todayBytes += (rxSpeed + txSpeed);
                    saveTodayData();

                    alertService.checkUsage(todayBytes);

                    netWidget.setImage(WidgetRenderer.render(
                            currentMode,
                            formatSpeed(rxSpeed),
                            formatSpeed(txSpeed),
                            todayBytes,
                            alertService.getAlertLimitBytes(),
                            caffeinateService.isCaffeinated(),
                            rxSpeed,
                            txSpeed
                    ));
                }
                previousRx = curRx;
                previousTx = curTx;

                // --- تحديث المعالج والرام ---
                double cpu = cpuMemoryService.getCpuUsage();
                long usedMem = cpuMemoryService.getUsedMemory();
                long totalMem = cpuMemoryService.getTotalMemory();

                cpuWidget.setImage(CpuWidgetRenderer.render(cpu, usedMem, totalMem));
                // تحديث قائمة البرامج المستهلكة في الخلفية كل ثانية
                cpuWidget.setPopupMenu(CpuMenuBuilder.build(cpuMemoryService));
            }
        }, 0, 1000);
    }

    private static void refreshNet(TrayIcon widget, CaffeinateService caffeinate, AlertService alerts, SystemInfoService sysInfo) {
        widget.setPopupMenu(MenuBuilder.build(caffeinate, alerts, sysInfo, currentMode, 
            () -> refreshNet(widget, caffeinate, alerts, sysInfo), 
            () -> {
                todayBytes = 0;
                saveTodayData();
                alerts.resetAlert();
                widget.setImage(WidgetRenderer.render(currentMode, "0 B", "0 B", 0, alerts.getAlertLimitBytes(), caffeinate.isCaffeinated(), 0, 0));
            },
            m -> {
                currentMode = m;
                refreshNet(widget, caffeinate, alerts, sysInfo);
            }
        ));
    }

    private static String formatSpeed(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        return String.format("%.1f %c", bytes / Math.pow(1024, exp), "KMGTPE".charAt(exp - 1));
    }

    private static void loadTodayData() {
        savedDate = prefs.get("date", LocalDate.now().toString());
        if (savedDate.equals(LocalDate.now().toString())) {
            todayBytes = prefs.getLong("bytes", 0);
        }
    }

    private static void saveTodayData() {
        prefs.put("date", LocalDate.now().toString());
        prefs.putLong("bytes", todayBytes);
    }

    private static void checkMidnightReset(AlertService alertService) {
        String today = LocalDate.now().toString();
        if (!today.equals(savedDate)) {
            todayBytes = 0;
            savedDate = today;
            saveTodayData();
            alertService.resetAlert();
        }
    }
}
```

---

### التجربة والاختبار:
1. قم بتشغيل البرنامج عبر زر التشغيل ▶️ في IntelliJ.
2. ستجد **ويدجتين مستقلتين** في شريط الماك:
    * **الأولى (الشبكة):** تضغط عليها بالزر الأيمن تفتح لك إعدادات الكافيين والتنبيهات.
    * **الثانية (المعالج والرام):** يظهر لك `⚡ 12% │ 🧠 7.8G` بألوان حية!
3. **جرب الضغط بالزر الأيمن على ويدجت المعالج:**
    * ستجد قائمة تعرض لك أكثر 5 برامج تستهلك المعالج وأكثر 5 برامج تستهلك الرام حالياً على جهازك!
    * ادخل على أي برنامج واضغط `Force Kill` لتجربة إغلاقه فوراً!
4. تذكر أنه يمكنك الضغط مع زر `⌘ Command` لسحب أي من الأيقونتين وإعادة ترتيبهما كما تحب!

جرب هذه المرحلة الأولى وأخبرني برأيك لننتقل فوراً إلى **المرحلة 2 (تتبع استهلاك التطبيقات للشبكة وحظرها)**! 🤩🚀