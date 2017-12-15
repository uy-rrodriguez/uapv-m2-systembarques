EMU=G:\Android-SDK\emulator\emulator.exe
EMUNAME=2.7_QVGA_slider_API_25
ADB=G:/Android-SDK/platform-tools/adb.exe
APPPATH=G:\Dev\RepoGit\uapv-m2-systembarques\TP2_Meteo2
LOGLABEL=TP2_Meteo
APK=$(APPPATH)\app\build\outputs/apk/app-debug.apk
GRADLEW=$(APPPATH)\gradlew.bat

default: build-install
all: run-emu build install

build:
	@echo "---------------- Build APK ----------------"
	#cd "$(APPPATH)"
	$(GRADLEW) assembleDebug
	#cd ..

build-install:
	@echo "---------------- Build-Install APK ----------------"
	#cd "$(APPPATH)"
	$(GRADLEW) installDebug
	#cd ..

run-emu:
	@echo "---------------- Run emulator ----------------"
	"$(EMU)" -avd "$(EMUNAME)"

install:
	@echo "---------------- Install APK ----------------"
	$(ADB) install "$(APK)"

log:
	@echo "---------------- Logs Android ----------------"
	$(ADB) logcat ActivityManager:E AndroidRuntime:E $(LOGLABEL):D *:S

log-clean:
	@echo "---------------- Clean Logs Android ----------------"
	$(ADB) logcat -c

clean:
	@echo "---------------- Clean start ----------------"
	rm "$(APK)"
	$(GRADLEW) clean
	@echo "---------------- Clean end ----------------"

