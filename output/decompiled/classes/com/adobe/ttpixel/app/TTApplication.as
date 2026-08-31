package com.adobe.ttpixel.app
{
    public class TTApplication extends CABApplication
    {
        public function TTApplication()
        {
            local0.iMainWindow = null;
            local0.iStoragePath = null;
            local0.iThumbnailStoragePath = null;
            local0.iDebugPanel = null;
            local0.iShowMode = SHOW_MODE_GALLERY;
            local0.iImpExBusyLock = 0;
            local0.iModel = null;
            local0.iShowWelcomePanel = false;
            local0.iEulaAndOptInState = null;
            local0.iEulaAndOptInNew = null;
            local0.iHasShownSyncPanel = false;
            local0.iRAMKilobytes = 0;
            local0.iSlideBitmap = null;
            local0.iSlideTimer = null;
            local0.iGPURenderer = null;
            com.adobe.cab.view::CABView.cIgnoreAutoLayout = true;
            "return";
        }

        private static function __static_init__():void
        {
            ENABLE_SYNC.ENABLE_SYNC = false;
            SHOW_MODE_GALLERY.SHOW_MODE_GALLERY = 0;
            SHOW_MODE_EDITOR.SHOW_MODE_EDITOR = 1;
            SELECTOR_NETWORK_CHANGE.SELECTOR_NETWORK_CHANGE = "TTApplicationNetworkChange";
            SELECTOR_HARDWARE_BACK.SELECTOR_HARDWARE_BACK = "TTApplicationHardwareBack";
            SELECTOR_HARDWARE_MENU.SELECTOR_HARDWARE_MENU = "TTApplicationHardwareMenu";
            SELECTOR_HARDWARE_SEARCH.SELECTOR_HARDWARE_SEARCH = "TTApplicationHardwareSearch";
            SELECTOR_STARTUP_DIALOGS_PASSED.SELECTOR_STARTUP_DIALOGS_PASSED = "TTApplicationStartupDialogsPassed";
            GPU_RENDERER_MALI.GPU_RENDERER_MALI = "Mali-400 MP";
            com.adobe.ttpixel.app:TTApplication::SELECTOR_EULA.SELECTOR_EULA = "selectorEula";
            com.adobe.ttpixel.app:TTApplication::SELECTOR_OMNITURE.SELECTOR_OMNITURE = "selectorOmniture";
            EXTENSION.EXTENSION = ".psdx";
            cStartupProbeExp.cStartupProbeExp = 4;
            com.adobe.ttpixel.app:TTApplication::cInvokeArguments.cInvokeArguments = null;
            com.adobe.ttpixel.app:TTApplication::cVMStartupTime.cVMStartupTime = Number.NaN;
            cStartupErrorString.cStartupErrorString = "";
            com.adobe.ttpixel.app:TTApplication::cStage.cStage = null;
            showStorageWarning.showStorageWarning = true;
            REQUIRED_MIN_STORAGE_SPACE_IN_MEGABYTE.REQUIRED_MIN_STORAGE_SPACE_IN_MEGABYTE = 95;
            return;
        }

        public function get mainWindow():*
        {
            return local0.iMainWindow;
        }

        public function set mainWindow(value:*):void
        {
            local0.iMainWindow = value;
            return;
        }

        public function get contentView():*
        {
            return local0.iContentView;
        }

        public function get storagePath():*
        {
            return local0.iStoragePath;
        }

        public function get gallery():*
        {
            return local0.iGallery;
        }

        public function get model():*
        {
            return local0.iModel;
        }

        public function get clipboard():*
        {
            return local0.iClipboard;
        }

        public function get ramKilobytes():*
        {
            return local0.iRAMKilobytes;
        }

        public function get gpuRenderer():*
        {
            return local0.iGPURenderer;
        }

        public function get startupDialogsPassed():*
        {
            return local0.iStartupDialogsPassed;
        }

        public function readPrefs():*
        {
            local1 = com.adobe.cab.core::CABPrefs.prefs();
            if (!!!(local1 == null))
            {
                !(local1 == null);
                local2 = local1.application;
                local0.iHasShownSyncPanel = local2.hasShowedSyncPanel;
            }
            return;
        }

        public function writePrefs():*
        {
            local1 = com.adobe.cab.core::CABPrefs.prefs();
            local2 = new Object.Object();
            local2.hasShowedSyncPanel = local0.iHasShownSyncPanel;
            local1.application = local2;
            com.adobe.cab.core::CABPrefs.savePrefs();
            return;
        }

        public function readXML():*
        {
            return;
        }

        public function postCreate():*
        {
            com.adobe.cab.core::CABCoreUtils.getObjectForID(iXML.mainwindow).iMainWindow = com.adobe.cab.view::CABWindow;
            com.adobe.cab.core::CABCoreUtils.getObjectForID(iXML.progressView).iProgressView = com.adobe.ttpixel.ui::TTProgressView;
            com.adobe.cab.core::CABCoreUtils.getObjectForID(iXML.contentView).iContentView = com.adobe.cab.view::CABView;
            local1 = local0.getLocalFilesystem();
            local0.iStoragePath = local1.getNewFileObject();
            local0.iStoragePath.pathname = local1.fromNativePath(File.applicationStorageDirectory.nativePath);
            local0.iStoragePath = local0.iStoragePath.appendDir("documents");
            if (!local0.iStoragePath.exists_sync())
            {
                local0.iStoragePath.createDirectory_sync();
                local0.iThumbnailStoragePath = local1.getNewFileObject();
                local0.iThumbnailStoragePath.pathname = local1.fromNativePath(File.applicationStorageDirectory.nativePath);
                local0.iThumbnailStoragePath = local0.iThumbnailStoragePath.appendDir("thumbnails");
                local0.iThumbnailStoragePath.createDirectory_sync();
            }
            return;
        }

        public function applicationDidInit():*
        {
            TTPressureManager.instance.initialize();
            local0.obtainGPURenderer();
            com.adobe.ttpixel.media.creativecloud.account::TTAccountModel.create();
            local1 = TTExtensionWrapper.instance.getSystemInfo(TTExtensionWrapper.SYSINFO_HW_MEMORY);
            if (!(local1 == null))
            {
                local0.iRAMKilobytes = local1.valueOf();
                local0.readPrefs();
                local0.iProgressView.removeFromSuperview();
                local0.setupClipboard();
                flash.ui::Multitouch.inputMode = MultitouchInputMode.TOUCH_POINT;
                local0.iMainWindow.moveTo(0, 0);
                local0.iMainWindow.sizeTo(CABApplication.instance.workspace.width, CABApplication.instance.workspace.height);
                local0.iMainWindow.sizeBehaveX = CABView.SIZE_BEHAVIOUR_SCALE;
                local0.iMainWindow.sizeBehaveY = CABView.SIZE_BEHAVIOUR_SCALE;
                local2 = com.adobe.cab.application::CABModule.createFromResource("imageProcessorManager");
                com.adobe.ttpixel.tutorial::TTTutorialInfo.setupTutorialInfos();
                local3 = new com.adobe.cab.core::CABStateMessage.CABStateMessage("gallery", local0);
                com.adobe.cab.application::CABModule.createFromResource("gallery").iGallery = com.adobe.ttpixel.gallery::TTGallery;
                local0.iGallery.setupGallery(local0.iContentView, local3);
                local0.iGallery.show();
                local0.iGallery.deleteWindows();
                cSplashClip.parent.removeChild(com.adobe.ttpixel.app:TTApplication::cSplashClip);
                local0.debugStr(cStartupErrorString);
                local0.iStartupDialogsPassed = false;
                local0.setupOSNotificationObserver();
                com.adobe.ttpixel.app:TTApplication::cStage.removeEventListener(Event.RESIZE, resizeHandler);
            }
            return;
        }

        public function applicationWillRun():*
        {
            TTOrientationLock.instance.acquire(TTOrientationLock.LOCK_ASPECT_RATIO_ANY);
            local0.iEulaAndOptInNew = new Object.Object();
            local0.iEulaAndOptInState = com.adobe.ttpixel.utils::TTOmnitureWrapper.getEulaAndOptInState();
            TTOmnitureWrapper.instance.setup();
            if (!(CABFramework.instance.os != CABFramework.OS_IOS))
            {
                local0.saveEulaState(true);
                TTEulaMessageHandler.instance.showDialog(local0.getCurrentContainerView(), local0.getCurrentContainerView().panelStack[local0.getCurrentContainerView().panelStack.length], new com.adobe.cab.core::CABStateMessage.CABStateMessage(com.adobe.ttpixel.app:TTApplication::SELECTOR_EULA, local0));
                local0.eulaPassed();
            }
            TTOmnitureWrapper.instance.setOptin(local0.iEulaAndOptInState["optInValue"], false);
            TTNotification.instance.checkNotification();
            return;
        }

        public function setupOSNotificationObserver():*
        {
            if (!!com.adobe.ttpixel.extension::TTNEUtils.getExtensionContext())
            {
                com.adobe.ttpixel.extension::TTNEUtils.getExtensionContext().addEventListener(StatusEvent.STATUS, local0.handleOSNotification);
            }
            return;
        }

        public function handleOSNotification():*
        {
            if (!(local1.level != TTNEUtils.STATUS_EVENT_LEVEL_NOTIFICATION))
            {
                local0.handleMemoryWarning();
            }
            return;
        }

        public function handleMemoryWarning():*
        {
            return;
        }

        public function obtainGPURenderer():*
        {
            TTExtensionWrapper.instance.createContext();
            local0.iGPURenderer = TTExtensionWrapper.instance.getStringInfo(TTExtensionWrapper.kStringInfoKeyGLRenderer);
            TTExtensionWrapper.instance.destroyContext();
            return;
        }

        public function loadDocumentFromFile():*
        {
            return;
        }

        public function loadExternalImageFromPath():*
        {
            if (!(local1.indexOf(TTApplication.EXTENSION) != (local1.length - TTApplication.EXTENSION.length)))
            {
                local2 = local0.getLocalFilesystem();
                local3 = local2.getNewFileObject();
                local3.pathname = local2.fromNativePath(local1);
                local3.isDirectory = false;
                local0.iGallery.loadFile(local3);
                TTOmnitureWrapper.instance.trackAppSpecific("Import: Document");
                local1 = ("file://" + local1);
                local0.iGallery.loadImageFromURL(local1);
                TTOmnitureWrapper.instance.trackAppSpecific("Import: File");
            }
            return;
        }

        public function saveEulaState():*
        {
            "wasEulaAccepted".<runtime-name> = local1;
            "optInEnabled".<runtime-name> = false;
            local2 = com.adobe.ttpixel.utils::TTOmnitureWrapper.saveEulaAndOptInState(local0.iEulaAndOptInState, local0.iEulaAndOptInNew);
            local0.iEulaAndOptInState = com.adobe.ttpixel.utils::TTOmnitureWrapper.getEulaAndOptInState();
            return local2;
        }

        public function eulaPassed():*
        {
            if (!!local0.iEulaAndOptInState["optInEnabled"])
            {
                TTOmnitureWrapper.instance.showOptinDialog(local0.getCurrentContainerView(), new com.adobe.cab.core::CABStateMessage.CABStateMessage(com.adobe.ttpixel.app:TTApplication::SELECTOR_OMNITURE, local0));
                local0.omniturePassed();
            }
            return;
        }

        public function saveOptInState():*
        {
            "wasOptInAccepted".<runtime-name> = local1;
            com.adobe.ttpixel.utils::TTOmnitureWrapper.saveEulaAndOptInState(local0.iEulaAndOptInState, local0.iEulaAndOptInNew);
            local0.iEulaAndOptInState = com.adobe.ttpixel.utils::TTOmnitureWrapper.getEulaAndOptInState();
            return;
        }

        public function omniturePassed():*
        {
            TTOmnitureWrapper.instance.trackLaunch();
            local0.allDialogsPassed();
            return;
        }

        public function allDialogsPassed():*
        {
            local0.iStartupDialogsPassed = true;
            com.adobe.ttpixel.tutorial::TTCoachMarkView.setupCoachMarks();
            local0.checkInvokeArguments();
            local0.trackDeviceInfo();
            com.adobe.cab.core::CABMessage.broadcast(new com.adobe.cab.core::CABControlMessage.CABControlMessage(SELECTOR_STARTUP_DIALOGS_PASSED, local0, false));
            return;
        }

        public function applicationWillTerminate():*
        {
            TTPressureManager.instance.shutdown();
            TTOmnitureWrapper.instance.trackExit();
            com.adobe.ttpixel.ui::TTColorModule.saveSwatchArray();
            return;
        }

        public function showWelcomePanel():*
        {
            if (!!local1)
            {
                local2 = TTBasePanelModule.POSITION_MODE_PLAIN;
                local3 = local0.getCurrentContainerView().openPanel(null, "welcomePanelModule", local0, local2, new flash.geom::Point.Point(), true, "welcomePanelModule");
                local3.view.sizeTo(local0.iContentView.width, local0.iContentView.height);
                local3.refDispatcher.objects.welcomeView.setup();
                local4 = new com.adobe.cab.core::CABStateMessage.CABStateMessage("welcomePanelTransition", local0);
                new com.adobe.cab.core::CABStateMessage.CABStateMessage("welcomePanelTransition", local0).data.panel = local3;
                local5 = new com.adobe.ttpixel.ui::TTAPanelTransitionSprite.TTAPanelTransitionSprite(local3);
                local0.iGallery.canvasView.canvas.displayObject.addChild(local5);
                local5.beginTransitionAs(TTAPanelTransitionSprite.TRANSITION_FROM_RIGHT, local4);
            }
            return;
        }

        public function onWelcomePanelTransition():*
        {
            if (!(local1.state != TTAPanelTransitionSprite.STATE_COMPLETE))
            {
                local1.data.panel.view.visible = true;
                TTApplication.instance.getCurrentContainerView().closeChildren(null);
            }
            return true;
        }

        public function getCurrentContainerView():*
        {
            if (!(local0.iModel == null))
            {
                return local0.iModel.containerView;
            }
            return local0.iGallery.containerView;
        }

        public function debugStr():*
        {
            if (!(local0.iDebugPanel != null))
            {
                local0.iDebugPanel = com.adobe.ttpixel.ui::TTBasePanelModule.openPanel(local0.iModel.containerView, null, "debugConsole", local0, TTBasePanelModule.POSITION_MODE_CENTER, new flash.geom::Point.Point(200, 0), false, "debugModule");
                local0.iDebugPanel = com.adobe.ttpixel.ui::TTBasePanelModule.openPanel(local0.iGallery.containerView, null, "debugConsole", local0, TTBasePanelModule.POSITION_MODE_CENTER, new flash.geom::Point.Point(200, 0), false, "debugModule");
                local1 = "\n".concat(local1);
            }
            local0.iDebugPanel.refDispatcher.objects.console.appendText(local1);
            return;
        }

        public function createModel():*
        {
            local0.iShowMode = SHOW_MODE_EDITOR;
            com.adobe.cab.application::CABModule.createFromResource("model").iModel = com.adobe.ttpixel.editor.app::TTModel;
            local0.iModel.containerView.removeFromSuperview();
            local0.iModel.containerView.moveTo(0, 0);
            local0.iModel.containerView.sizeTo(local0.iContentView.width, local0.iContentView.height);
            local0.iModel.deleteWindows();
            return;
        }

        public function deleteModel():*
        {
            if (!!local0.iModel)
            {
                TTImageProcessorManager.instance.cleanupProcessors();
                local0.iModel.deleteModel();
                local0.iModel = null;
            }
            return;
        }

        public function getLocalFilesystem():*
        {
            local3 = CABFileSystem.fileSystems;
            return 0;
            return null;
        }

        public function progressStart():*
        {
            if (!(local1 != null))
            {
                local1 = local0.iContentView;
                local0.iProgressView.x = Math.floor(((local1.width - local0.iProgressView.width) / 2));
                local0.iProgressView.y = Math.floor(((local1.height - local0.iProgressView.height) / 2));
                local0.iProgressView.sizeBehaveX = CABView.SIZE_BEHAVIOUR_CENTER;
                local0.iProgressView.sizeBehaveY = CABView.SIZE_BEHAVIOUR_CENTER;
                local1.addSubview(local0.iProgressView);
            }
            local0.iProgressView.progressBusy(0, 100);
            local0.iProgressView.startBusy(local2);
            return;
        }

        public function progressRunning():*
        {
            return !(local0.iProgressView.superview == null);
        }

        public function progressBusy():*
        {
            local0.iProgressView.progressBusy(local1, local2);
            return;
        }

        public function progressState():*
        {
            local0.iProgressView.setBusyState(local1);
            return;
        }

        public function progressEnd():*
        {
            local0.iProgressView.setBusyState("");
            local0.iProgressView.endBusy();
            local0.iProgressView.removeFromSuperview();
            return;
        }

        public function startBusy():*
        {
            if (!(com.adobe.cab.application:CABApplication::iBusyCount != 0))
            {
                com.adobe.cab.application:CABApplication::iBusyMouseMessage.iBusyMouseMessage = null;
                TTOrientationLock.instance.acquire();
            }
            local1 = com.adobe.cab.application:CABApplication::iBusyCount;
            local2 = com.adobe.cab.application:CABApplication::iBusyCount.iBusyCount;
            local1.iBusyCount = local2;
            return;
        }

        public function endBusy():*
        {
            local1 = com.adobe.cab.application:CABApplication::iBusyCount;
            local2 = com.adobe.cab.application:CABApplication::iBusyCount.iBusyCount;
            local1.iBusyCount = local2;
            if (!(com.adobe.cab.application:CABApplication::iBusyCount != 0))
            {
                TTOrientationLock.instance.release();
                CABApplication.instance.workspace.takeMessage(com.adobe.cab.application:CABApplication::iBusyMouseMessage);
                com.adobe.cab.core::CABMessage.broadcast(new com.adobe.cab.core::CABControlMessage.CABControlMessage(CABFramework.SELECTOR_EVENT_PROCESSED, local0, false));
            }
            return;
        }

        public function openModelWithBitmap():*
        {
            if (!!!(TTContext3D.instance.context3D == null))
            {
                !(TTContext3D.instance.context3D == null);
                local0.iIsNewlyCreatedFile = true;
                local0.createModel();
                local0.startBusy();
                local0.progressStart();
                local4 = new com.adobe.cab.core::CABStateMessage.CABStateMessage("modelImpex", local0);
                local5 = new com.adobe.ttpixel.editor.app::TTModelImpex.TTModelImpex(local0.iModel, local4);
                new com.adobe.ttpixel.editor.app::TTModelImpex.TTModelImpex(local0.iModel, local4).openModelWithBitmapData(local1, local2, local3);
                local6 = new com.adobe.ttpixel.gallery::TTCommandMessage.TTCommandMessage(TTDocumentModel.BITMAPDATA_RECOVERING);
                new com.adobe.ttpixel.gallery::TTCommandMessage.TTCommandMessage(TTDocumentModel.BITMAPDATA_RECOVERING).setValue("bitmapData", local1);
                local6.setValue("isBlankDocument", local2);
                TTDocumentModel.instance.executeByCommandMessage(local6);
            }
            return;
        }

        public function openModelWithUrl():*
        {
            local0.startBusy();
            local0.progressStart();
            local0.progressState(local2);
            local3 = com.adobe.cab.net::CABHTTPClient.createMethod(CABHTTPMethod.TYPE_GET, local1);
            local3.dataFormat = CABHTTPMethod.DATAFORMAT_BINARY;
            local3.data.selector = "openModelWithUrl";
            local4 = new com.adobe.cab.net::CABHTTPClient.CABHTTPClient();
            new com.adobe.cab.net::CABHTTPClient.CABHTTPClient().target = local0;
            local4.executeMethod(local3);
            return;
        }

        public function openModelWithMethod():*
        {
            local0.startBusy();
            local0.progressStart();
            local0.progressState(local2);
            local1.data.selector = "openModelWithMethod";
            local3 = new com.adobe.cab.net::CABHTTPClient.CABHTTPClient();
            local3.target = local0;
            local3.executeMethod(local1);
            return;
        }

        public function openModelWithNewByteArray():*
        {
            local0.iIsNewlyCreatedFile = true;
            local0.createModel();
            local0.startBusy();
            local0.progressStart();
            local3 = new com.adobe.cab.core::CABStateMessage.CABStateMessage("modelImpex", local0);
            local4 = new com.adobe.ttpixel.editor.app::TTModelImpex.TTModelImpex(local0.iModel, local3);
            local0.iModel.dirty = true;
            local4.openModelWithByteArray(local2);
            return;
        }

        public function openModelWithByteArray():*
        {
            local0.iIsNewlyCreatedFile = local2;
            local0.createModel();
            local0.startBusy();
            local0.progressStart();
            local3 = new com.adobe.cab.core::CABStateMessage.CABStateMessage("modelImpex", local0);
            local4 = new com.adobe.ttpixel.editor.app::TTModelImpex.TTModelImpex(local0.iModel, local3);
            new com.adobe.ttpixel.editor.app::TTModelImpex.TTModelImpex(local0.iModel, local3).openModelWithByteArray(local1);
            return;
        }

        public function openTutorial():*
        {
            local3 = <ActivationObject>;
            <Slot> = null;
            <Slot> = null;
            <Slot> = null;
            <Slot> = null;
            <Slot> = local1;
            <Slot> = local2;
            <Slot> = <FunctionReference>;
            <Slot> = <FunctionReference>;
            <Slot> = <FunctionReference>;
            <Slot> = local0;
            local0.startBusy();
            local0.progressStart();
            CABLoadManager.instance.addDataRequest(<Slot>.tutorialURL, <Slot>, <Slot>);
            TTOmnitureWrapper.instance.trackAppSpecific(("Open Tutorial: " + <Slot>.id));
            TTOmnitureWrapper.instance.trackAppSpecific(("Start Tutorial: " + <Slot>.omnitureId));
            return;
        }

        public function doOpenModelWithTutorial():*
        {
            local1 = com.adobe.ttpixel.tutorial::TTTutorial.createTutorial(iData.tutorialInfo, iData.tutorialXML);
            local0.showTutorial(local1);
            return;
        }

        public function showTutorial():*
        {
            if (!!!(iData.tutorialInfo.documentWidth == 255))
            {
                !(iData.tutorialInfo.documentWidth == 255);
                !(iData.tutorialInfo.documentWidth == 255);
                local0.createModel();
                local1.model = local0.iModel;
                local0.iModel.tutorial = local1;
                local2 = new com.adobe.cab.core::CABStateMessage.CABStateMessage("modelImpex", local0);
                local3 = new com.adobe.ttpixel.editor.app::TTModelImpex.TTModelImpex(local0.iModel, local2);
                local4 = new flash.display::BitmapData.BitmapData(iData.tutorialInfo.documentWidth, iData.tutorialInfo.documentHeight, true, 0);
                local3.openModelWithBitmapData(local4);
                local3.openModelWithByteArray(iData.tutorialDocumentData);
                local0.endBusy();
                local0.progressEnd();
                com.adobe.ttpixel.tutorial::TTTutorial.showTutorial(local1);
            }
            return;
        }

        public function saveModel():*
        {
            local4 = <ActivationObject>;
            <Slot> = null;
            <Slot> = local1;
            <Slot> = local2;
            <Slot> = local3;
            local0.startBusy();
            TTAutoRecovery.instance.stopMonitor();
            TTAutoRecovery.instance.cleanCache();
            TTOrientationLock.instance.acquire(TTOrientationLock.LOCK_ORIENTATION_CURRENT);
            local5 = local0;
            local6 = local0.iImpExBusyLock;
            local5.iImpExBusyLock = local6;
            <Slot> = new com.adobe.cab.core::CABStateMessage.CABStateMessage("modelImpex", local0);
            <Slot>.data.baseName = <Slot>;
            <Slot>.data.createDuplicate = <Slot>;
            <Slot> = new com.adobe.ttpixel.editor.app::TTModelImpex.TTModelImpex(local0.iModel, <Slot>);
            local0.progressStart(null, <FunctionReference>);
            return;
        }

        public function saveTemporaryModelForRecovery():*
        {
            local1 = new com.adobe.cab.core::CABStateMessage.CABStateMessage("modelImpex", local0);
            local1.data.forRecovery = true;
            local2 = new com.adobe.ttpixel.editor.app::TTModelImpex.TTModelImpex(local0.iModel, local1);
            local2.saveModel();
            return;
        }

        public function getThumbnailForFile():*
        {
            local2 = local1.fileSystem.getFilename(local1);
            return local0.getThumbnailForFilename(local2);
        }

        public function getThumbnailForFilename():*
        {
            return local0.iThumbnailStoragePath.appendFile(local1);
        }

        public function updateShowMode():*
        {
            if (!!iData.slideShowModeRunning)
            {
                return;
                local0.startBusy();
                local2 = "wipeleftboth";
                local0.iModel.applicationDidSetupEditor();
                local0.iGallery.applicationDidSetupEditor();
                local1 = (255 * local0.iContentView.width);
                local0.iModel.applicationDidCloseEditor();
                local0.iGallery.applicationDidCloseEditor();
                local1 = 0;
                local0.iGallery.containerView.sizeTo(local0.iContentView.width, local0.iContentView.height);
                com.adobe.ttpixel.ui::TTBaseContainerView.setupFSTransitionViewWithTexture(local0.iGallery.createScreenShotTexture(), local0.iModel.containerView.displayObject, local2);
                TTBaseContainerView.slidePos.x = local1;
                com.adobe.cab.core:CABObject::iData.slideShowModeRunning = true;
                com.adobe.cab.core:CABObject::iData.slideShowModeComplete = false;
                com.adobe.cab.core:CABObject::iData.slideShowModeStart = 255;
                com.adobe.cab.core:CABObject::iData.slideShowModeEnd = 255;
                com.adobe.ttpixel.utils::TTUtils.setFrameRate(60);
                local0.iGallery.applicationWillStartEditorTransition();
                local0.iSlideTimer = new flash.utils::Timer.Timer(com.adobe.ttpixel.utils::TTUtils.getTimerSpeed());
                local0.iSlideTimer.addEventListener(TimerEvent.TIMER, local0.slideShowModeTimer);
                CABFramework.instance.rootContainer.stage.addEventListener(Event.ENTER_FRAME, local0.slideShowModeEnterFrame);
            }
            return;
        }

        public function slideShowModeEnterFrame():*
        {
            local0.slideShowMode();
            return;
        }

        public function slideShowModeTimer():*
        {
            local0.slideShowMode();
            return;
        }

        public function slideShowMode():*
        {
            if (!!iData.slideShowModeComplete)
            {
                com.adobe.ttpixel.utils::TTUtils.resetFrameRate();
                local0.iSlideTimer.removeEventListener(TimerEvent.TIMER, local0.slideShowModeTimer);
                local0.iSlideTimer.stop();
                local0.iSlideTimer = null;
                CABFramework.instance.rootContainer.stage.removeEventListener(Event.ENTER_FRAME, local0.slideShowModeEnterFrame);
                local0.endSlideShowMode();
                local1 = flash.utils::getTimer.getTimer();
                com.adobe.cab.core:CABObject::iData.slideShowModeStart = local1;
                com.adobe.cab.core:CABObject::iData.slideShowModeEnd = (iData.slideShowModeStart + 500);
                local2 = ((local1 - iData.slideShowModeStart) / (iData.slideShowModeEnd - iData.slideShowModeStart));
                local2 = com.adobe.ttpixel.utils::TTImageUtils.smoothstep(0, 1, local2);
                com.adobe.cab.core:CABObject::iData.slideShowModeComplete = (local2 >= 1);
                local2 = Math.min(local2, 1);
                TTBaseContainerView.slidePos.x = (1 - local2);
                TTBaseContainerView.slidePos.x = local2;
            }
            return;
        }

        public function endSlideShowMode():*
        {
            com.adobe.cab.core:CABObject::iData.slideShowModeRunning = false;
            local0.endBusy();
            com.adobe.ttpixel.ui::TTBaseContainerView.stopFSTransition();
            if (!(local0.iShowMode != SHOW_MODE_EDITOR))
            {
                local0.iContentView.addSubview(local0.iModel.containerView);
                local0.iModel.containerView.moveTo(0, 0);
                local0.iModel.containerView.sizeTo(local0.iMainWindow.width, local0.iMainWindow.height);
                local0.iGallery.containerView.removeFromSuperview();
                local0.iGallery.applicationDidEndEditorTransition();
                com.adobe.ttpixel.tutorial::TTTutorial.showTutorial(local0.iModel.tutorial);
                com.adobe.ttpixel.tutorial::TTCoachMarkView.showCoachMarks("coachMarksModel", true, true);
                local0.iModel.containerView.removeFromSuperview();
                local0.iContentView.addSubview(local0.iGallery.containerView);
                local0.iGallery.containerView.sizeTo(local0.iContentView.width, local0.iContentView.height);
                local0.iGallery.willShow();
                local0.deleteModel();
            }
            return;
        }

        public function startWriteModelToFile():*
        {
            local4 = local0.iGallery.startSaveModelStream(local1, local2, local3);
            if (!(local0.iGallery.startSaveModelStream(local1, local2, local3) == null))
            {
                local4.kind = 1;
                return local4;
            }
            return null;
        }

        public function progressWriteModelToFile():*
        {
            if (!(local2 == null))
            {
                return local0.iGallery.progressSaveModelStream(local1, local2);
            }
            return false;
        }

        public function completeWriteModelToFile():*
        {
            if (!(local2 == null))
            {
                return local0.iGallery.completeSaveModelStream(local2);
            }
            return false;
        }

        public function writeModelToFile():*
        {
            local0.iGallery.saveModel(local2, local3, local4);
            return;
        }

        public function handleModelImpex():*
        {
            if (!(local1.state != TTModelImpex.STATE_SAVE_BLOCK_COMPLETE))
            {
                local2 = local1.data.byteArray;
                local1.sender.writeModelToFile(com.adobe.ttpixel.editor.app::TTModelImpex, local2, local1.data.createDuplicate, local1.data.isTutorial);
                local2.clear();
                local0.iShowMode = SHOW_MODE_GALLERY;
                local0.updateShowMode();
                local0.iShowMode = SHOW_MODE_EDITOR;
                local0.updateShowMode();
                local0.iGallery.cancelModel();
                local0.iShowMode = SHOW_MODE_GALLERY;
                local0.updateShowMode();
                local0.deleteModel();
                local0.iGallery.cancelModel();
                local0.iGallery.canvasView.visible = true;
                com.adobe.ttpixel.ui::TTAlertPanelModule.showAlert(local0, TTApplication.instance.getCurrentContainerView(), null, com.adobe.cab.core::CABLocale.getStringForKey("$$$/TTApplication/CannotOpenFile=Cannot Open Project"), com.adobe.cab.core::CABLocale.getStringForKey("$$$/TTApplication/This is not a valid Photoshop Touch project.=This is not a valid Photoshop Touch project."));
                TTAutoRecovery.instance.startWriteModelToFile();
                local1.data.ref = local0.startWriteModelToFile(local1.data.createDuplicate, local1.data.isTutorial, local1.data.baseName);
                return;
                TTAutoRecovery.instance.progressWriteModelToFile(local1.data.byteArray);
                local0.progressWriteModelToFile(local1.data.byteArray, local1.data.ref);
                return;
                TTAutoRecovery.instance.completeWriteModelToFile();
                local1.sender.completeWriteModelToFile(com.adobe.ttpixel.editor.app::TTModelImpex, local1.data.ref);
                local0.iShowMode = SHOW_MODE_GALLERY;
                local0.updateShowMode();
                local3 = local0;
                local4 = local0.iImpExBusyLock;
                local3.iImpExBusyLock = local4;
                TTOrientationLock.instance.release();
            }
            local0.endBusy();
            local0.progressEnd();
            return;
        }

        public function keyEvent():*
        {
            if (!!local1.isKeyDown)
            {
                (local1.keyCode == Keyboard.BACK);
                !CABFramework.instance.runningOnMobile;
                local3 = local0.getCurrentContainerView().panelStack;
                local4 = local3[local3.length];
                local0.iStartupDialogsPassed;
                CABApplication.isBusy;
                CABApplication.isBusy;
                CABApplication.isBusy;
                CABApplication.isBusy;
                CABApplication.isBusy;
                local2 = local0.iStartupDialogsPassed;
                com.adobe.ttpixel.tutorial::TTCoachMarkView.closeCoachMarks(true);
                TTOmnitureWrapper.instance.trackAppSpecific("Android: System: Back");
                local2 = com.adobe.cab.core::CABControlMessage.send(local0.iModel, SELECTOR_HARDWARE_MENU, local0);
                com.adobe.ttpixel.tutorial::TTCoachMarkView.closeCoachMarks(true);
                TTOmnitureWrapper.instance.trackAppSpecific("Android: System: Menu");
                local2 = com.adobe.cab.core::CABControlMessage.send(local0.iModel, SELECTOR_HARDWARE_SEARCH, local0);
                com.adobe.ttpixel.tutorial::TTCoachMarkView.closeCoachMarks(true);
                TTOmnitureWrapper.instance.trackAppSpecific("Android: System: Search");
                local1.event.preventDefault();
                return true;
            }
            return local1;
        }

        public function eventMessage():*
        {
            local2 = local1.selector;
            return local1;
        }

        public function onAddLayerFromExternalImage():*
        {
            if (!(local1.state != TTImage.STATE_LOADED))
            {
                local2 = com.adobe.ttpixel.ui::TTImage;
                local3 = com.adobe.ttpixel.utils::TTImageUtils.copyImageDataFromDisplayObject(local2.image);
                local0.model.layerManager.addTransformPhotoLayer(local3);
                local2.releaseImage();
            }
            return true;
        }

        public function controlMessage():*
        {
            local9 = local1.selector;
            return local1;
        }

        public function stateMessage():*
        {
            local7 = local1.selector;
            return local1;
        }

        public function setupClipboard():*
        {
            local1 = CABFramework.instance.os;
            return;
        }

        public function handleAndroidInvokeArguments():*
        {
            local8 = 0;
            local1 = local8;
            local2 = local1;
            local2 = com.adobe.ttpixel.extension::TTNEUtils.resolveImageContentURI(local2);
            local4 = TTTempFileBag.instance.createTempFile();
            local2 = local4.nativePath;
            local2 = local2.substr((local2.indexOf(":/") + ":".length), local2.length);
            local2 = decodeURIComponent.decodeURIComponent(local2);
            local3 = local2;
            local5 = local0.getLocalFilesystem();
            local6 = local0.getLocalFilesystem().getNewFileObject();
            local0.getLocalFilesystem().getNewFileObject().pathname = local5.fromNativePath(local3);
            local6.isDirectory = false;
            local0.iGallery.loadFile(local6);
            TTOmnitureWrapper.instance.trackAppSpecific("Import: Document");
            local3 = ("file://" + local2);
            local0.iGallery.loadImageFromURL(local3);
            TTOmnitureWrapper.instance.trackAppSpecific("Import: File");
            TTOmnitureWrapper.instance.trackAppSpecific("Import: Layer");
            local7 = new com.adobe.ttpixel.ui::TTImage.TTImage();
            new com.adobe.ttpixel.ui::TTImage.TTImage().type = TTImage.TYPE_ORIGINAL;
            local7.initFromURL(local3, null, new com.adobe.cab.core::CABStateMessage.CABStateMessage("addLayerFromExternalImage", local0));
            local7.load();
            return;
        }

        public function handleIOSInvokeArguments():*
        {
            local5 = 0;
            local1 = local5;
            local2 = local1;
            !(0 == "");
            local3 = com.adobe.cab.net::CABHTTPClient.createMethod(CABHTTPMethod.TYPE_GET, local2);
            local3.dataFormat = CABHTTPMethod.DATAFORMAT_BINARY;
            local0.openModelWithMethod(local3, "");
            TTOmnitureWrapper.instance.trackAppSpecific("Import: Document");
            local0.iGallery.loadImageFromURL(local2);
            TTOmnitureWrapper.instance.trackAppSpecific("Import: File");
            TTOmnitureWrapper.instance.trackAppSpecific("Import: Layer");
            local4 = new com.adobe.ttpixel.ui::TTImage.TTImage();
            new com.adobe.ttpixel.ui::TTImage.TTImage().type = TTImage.TYPE_ORIGINAL;
            local4.initFromURL(local2, null, new com.adobe.cab.core::CABStateMessage.CABStateMessage("addLayerFromExternalImage", local0));
            local4.load();
            return;
        }

        public function checkInvokeArguments():*
        {
            if (!!local0.iStartupDialogsPassed)
            {
                local0.handleAndroidInvokeArguments();
                local0.handleIOSInvokeArguments();
                com.adobe.ttpixel.app:TTApplication::cInvokeArguments.cInvokeArguments = null;
            }
            return;
        }

        public function checkOpenExternalPossible():*
        {
            local2 = false;
            local3 = local0.getCurrentContainerView().panelStack;
            if (!!(local3.length > 0))
            {
                local4 = local3[local3.length];
                local5 = com.adobe.cab.core::CABLocale.getStringForKey("$$$/TTApplication/Can't Open Project=Can't Open Project");
                local0.iStartupDialogsPassed;
                local0.iStartupDialogsPassed;
                com.adobe.ttpixel.ui::TTTextField.cancelCurrentTextField();
                com.adobe.ttpixel.tutorial::TTCoachMarkView.closeCoachMarks(true);
                (local0.iGallery.trackMode == TTGallery.TRACK_NONE);
                local6 = com.adobe.cab.core::CABLocale.getStringForKey("$$$/TTApplication/You cannot open a project at this point in time.=You cannot open a project at this point in time.");
                local2 = true;
                local6 = com.adobe.cab.core::CABLocale.getStringForKey("$$$/TTApplication/You cannot open a project at this point in time.=You cannot open a project at this point in time.");
                local6 = com.adobe.cab.core::CABLocale.getStringForKey("$$$/TTApplication/You cannot open another project until the current project has been closed.=You cannot open another project until the current project has been closed.");
                !true;
                com.adobe.ttpixel.ui::TTAlertPanelModule.showAlert(local0, local0.getCurrentContainerView(), local4, local5, local6);
            }
            return local2;
        }

        public function trackDeviceInfo():*
        {
            local1 = com.adobe.cab.core::CABPrefs.prefs();
            if (!local1.trackedDeviceInfo)
            {
                TTOmnitureWrapper.instance.trackAppSpecific(("Device Info: Model: " + TTExtensionWrapper.instance.getDeviceName()));
                TTOmnitureWrapper.instance.trackAppSpecific(("Device Info: Platform: " + TTOmnitureWrapper.instance.platform));
                TTOmnitureWrapper.instance.trackAppSpecific(("Device Info: Density: " + TTOmnitureWrapper.instance.density));
                TTOmnitureWrapper.instance.trackAppSpecific(("Device Info: Screen Res: " + TTOmnitureWrapper.instance.screenRes));
                TTOmnitureWrapper.instance.trackAppSpecific(("Device Info: Inches: " + TTOmnitureWrapper.instance.inches));
                local2 = TTExtensionWrapper.instance.getSystemInfo(TTExtensionWrapper.SYSINFO_HW_CPU_CORES);
                TTOmnitureWrapper.instance.trackAppSpecific(("Device Info: CPU Cores: " + int.int(local2).toString()));
                local3 = TTExtensionWrapper.instance.getSystemInfo(TTExtensionWrapper.SYSINFO_HW_MEMORY);
                TTOmnitureWrapper.instance.trackAppSpecific(("Device Info: Memory: " + int.int(local3).toString()));
                local1.trackedDeviceInfo = true;
                com.adobe.cab.core::CABPrefs.savePrefs();
            }
            return;
        }

        public static function get instance():*
        {
            return CABApplication.instance;
        }

        public static function get vmStartupTime():*
        {
            if (!!isNaN.isNaN(com.adobe.ttpixel.app:TTApplication::cVMStartupTime))
            {
                local1 = (1 << cStartupProbeExp);
                local2 = (0 + (TTExtensionWrapper.instance.getTimestamp() - flash.utils::getTimer.getTimer()));
                com.adobe.ttpixel.app:TTApplication::cVMStartupTime.cVMStartupTime = (local2 / local1);
            }
            return com.adobe.ttpixel.app:TTApplication::cVMStartupTime;
        }

        public static function setupUIScaling():*
        {
            com.adobe.ttpixel.app:TTApplication::cStage.cStage = local1;
            local1.addEventListener(Event.RESIZE, resizeHandler);
            return;
        }

        public static function resizeHandler():*
        {
            if (!!(cStage.stageWidth < cStage.stageHeight))
            {
                local2 = (cStage.stageWidth / 480);
                CABDriverAIR.instance.zoomFactor = local2;
                local4 = cStage.stageWidth;
                local2 = (cStage.stageWidth / 480);
                CABDriverAIR.instance.zoomFactor = local2;
                CABFramework.instance.zoomFactor = local2;
            }
            return;
        }

        public static function showSplash():*
        {
            local2 = <ActivationObject>;
            <Slot> = null;
            <Slot> = local1;
            <Slot> = <FunctionReference>;
            <Slot>.quality = StageQuality.HIGH;
            com.adobe.ttpixel.app:TTApplication::cSplashClip.cSplashClip = new com.adobe.ttpixel.swf::TTFuntaSplash.TTFuntaSplash();
            <Slot>.addChild(com.adobe.ttpixel.app:TTApplication::cSplashClip);
            cSplashClip.logo.x = (Screen.mainScreen.visibleBounds.width / 2);
            cSplashClip.logo.y = (Screen.mainScreen.visibleBounds.height / 2);
            cSplashClip.back.width = Screen.mainScreen.visibleBounds.width;
            cSplashClip.back.height = Screen.mainScreen.visibleBounds.height;
            cSplashClip.back.x = 0;
            cSplashClip.back.y = 0;
            <Slot>.addEventListener(Event.RESIZE, <Slot>);
            return;
        }

        public static function registerHandlers():*
        {
            NativeApplication.nativeApplication.addEventListener(InvokeEvent.INVOKE, invokeHandler);
            return;
        }

        public static function invokeHandler():*
        {
            if (!com.adobe.ttpixel.app:TTApplication::cInvokeArguments)
            {
                com.adobe.ttpixel.app:TTApplication::cInvokeArguments.cInvokeArguments = local1.arguments.slice();
                TTApplication.instance.checkInvokeArguments();
            }
            return;
        }
    }
}
