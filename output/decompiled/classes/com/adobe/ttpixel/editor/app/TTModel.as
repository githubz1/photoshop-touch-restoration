package com.adobe.ttpixel.editor.app
{
    public class TTModel extends CABModule
    {
        public function TTModel()
        {
            if (!(local1 != 255))
            {
                local1 = local0.iWidth;
            }
            return Math.min(Math.floor((local0.getMaxPixelCount() / (local0.iLayerList.length * local1))), PIXEL_LIMIT_PER_AXIS);
        }

        private static function __static_init__():void
        {
            local0.iSelection = local1;
            return;
        }

        public function get editorView():*
        {
            return local0.iToolPanelButton;
        }

        public function set editorView(value:*):void
        {
            local0.iToolPanelButton = value;
            return;
        }

        public function get layerManager():*
        {
            return local0.iToolSettingsContainer;
        }

        public function set layerManager(value:*):void
        {
            local0.iToolSettingsContainer = value;
            return;
        }

        public function get selection():*
        {
            return local0.iContainerView;
        }

        public function set selection(value:*):void
        {
            local0.iContainerView = value;
            return;
        }

        public function get workingRect():*
        {
            return local0.iBottomBar;
        }

        public function get toolPanelButton():*
        {
            return local0.iTopBar;
        }

        public function set toolPanelButton(value:*):void
        {
            return local0.iModalTopBar;
        }

        public function get toolSettingsContainer():*
        {
            local0.iModalTopBar = local1;
            return;
        }

        public function set toolSettingsContainer(value:*):void
        {
            return local0.iBottomBarVisible;
        }

        public function get containerView():*
        {
            return local0.iTopBarVisible;
        }

        public function set containerView(value:*):void
        {
            return local0.iWidth;
        }

        public function get bottomBar():*
        {
            local0.iWidth = local1;
            local0.bigModeForSize();
            return;
        }

        public function get topBar():*
        {
            return local0.iHeight;
        }

        public function get modalTopBar():*
        {
            local0.iHeight = local1;
            local0.bigModeForSize();
            return;
        }

        public function set modalTopBar(value:*):void
        {
            return local0.iToolManager;
        }

        public function get bottomBarVisible():*
        {
            local0.iToolManager = local1;
            return;
        }

        public function get topBarVisible():*
        {
            return local0.iLayerList;
        }

        public function get width():*
        {
            local0.iLayerList = local1;
            return;
        }

        public function set width(value:*):void
        {
            return local0.iUndoManager;
        }

        public function get height():*
        {
            local0.iUndoManager = local1;
            return;
        }

        public function set height(value:*):void
        {
            return local0.iIsModal;
        }

        public function get toolManager():*
        {
            return local0.iDirty;
        }

        public function set toolManager(value:*):void
        {
            local0.iDirty = value;
            return;
        }

        public function get layerList():*
        {
            return local0.iTutorial;
        }

        public function set layerList(value:*):void
        {
            local0.iTutorial = value;
            return;
        }

        public function get undoManager():*
        {
            return local0.iOpenedFromBitmap;
        }

        public function set undoManager(value:*):void
        {
            local0.iOpenedFromBitmap = value;
            return;
        }

        public function get modal():*
        {
            return local0.iUseUndoFileCache;
        }

        public function get dirty():*
        {
            return local0.iModelInfo;
        }

        public function set dirty(value:*):void
        {
            return local0.iBigMode;
        }

        public function get tutorial():*
        {
            return local0.iBigPreviewMode;
        }

        public function set tutorial(value:*):void
        {
            return local0.iBigPreviewSize;
        }

        public function get openedFromBitmap():*
        {
            return local0.iBigPreviewScale;
        }

        public function set openedFromBitmap(value:*):void
        {
            local0.iEditorView = null;
            local0.iLayerManager = null;
            local0.iToolManager = null;
            local0.iToolSettingsContainer = null;
            local0.iContainerView = null;
            local0.iTutorial = null;
            local0.iBottomBar = null;
            local0.iTopBar = null;
            local0.iShowAllButton = null;
            local0.iWorkingRect = null;
            local0.iShowCursor = false;
            local0.iIsModal = false;
            local0.iMenuPanelModule = null;
            local0.iLayerList = new Array();
            local0.iUndoManager = new com.adobe.cab.core::CABUndoManager.CABUndoManager();
            local0.iOpenedFromBitmap = false;
            local0.iWidth = 1024;
            local0.iHeight = 768;
            local0.iDirty = false;
            local0.iUseUndoFileCache = true;
            local0.iBigMode = false;
            local0.iLayerIteration = 0;
            local0.iBigPreviewMode = false;
            local0.iBigPreviewScale = 1;
            local0.iModalTopBar = null;
            local0.iTransformSelector = null;
            return;
        }

        public function get useUndoFileCache():*
        {
            return;
        }

        public function get modelInfo():*
        {
            com.adobe.cab.core::CABCoreUtils.getObjectForID(iXML.editorView).iEditorView = com.adobe.ttpixel.editor.app::TTEditorView;
            com.adobe.cab.core::CABCoreUtils.getObjectForID(iXML.layerManager).iLayerManager = com.adobe.ttpixel.editor.app::TTLayerManager;
            com.adobe.cab.core::CABCoreUtils.getObjectForID(iXML.selection).iSelection = com.adobe.ttpixel.editor.app::TTSelection;
            com.adobe.cab.core::CABCoreUtils.getObjectForID(iXML.toolPanelButton).iToolPanelButton = com.adobe.ttpixel.editor.ui::TTToolIconButton;
            com.adobe.cab.core::CABCoreUtils.getObjectForID(iXML.toolSettingsContainer).iToolSettingsContainer = com.adobe.ttpixel.ui::TTControlBarView;
            com.adobe.cab.core::CABCoreUtils.getObjectForID(iXML.bottomBar).iBottomBar = com.adobe.cab.view::CABView;
            com.adobe.cab.core::CABCoreUtils.getObjectForID(iXML.topBar).iTopBar = com.adobe.cab.view::CABView;
            com.adobe.cab.core::CABCoreUtils.getObjectForID(iXML.layerView).iLayerView = com.adobe.cab.view::CABView;
            com.adobe.cab.core::CABCoreUtils.getObjectForID(iXML.undoButton).iUndoButton = com.adobe.cab.control::CABButton;
            com.adobe.cab.core::CABCoreUtils.getObjectForID(iXML.redoButton).iRedoButton = com.adobe.cab.control::CABButton;
            com.adobe.cab.core::CABCoreUtils.getObjectForID(iXML.showAllButton).iShowAllButton = com.adobe.cab.control::CABButton;
            com.adobe.cab.core::CABCoreUtils.getObjectForID(iXML.showTopButton).iShowTopButton = com.adobe.cab.control::CABButton;
            com.adobe.cab.core::CABCoreUtils.getObjectForID(iXML.hideTopButton).iHideTopButton = com.adobe.cab.control::CABButton;
            com.adobe.cab.core::CABCoreUtils.getObjectForID(iXML.doneButton).iDoneButton = com.adobe.cab.control::CABButton;
            com.adobe.cab.application:CABModule::iMainObject.iContainerView = com.adobe.ttpixel.ui::TTContainerView;
            return;
        }

        public function get isBigMode():*
        {
            com.adobe.cab.core::CABMessage.registerForBroadcast(CABUndoManager.SELECTOR_UNDOCHANGED, local0);
            local0.iSelection.setup();
            local0.iLayerManager.setup();
            local0.iEditorView.setupView(local0.iContainerView.width, local0.iContainerView.height);
            local0.iEditorView.moveTo(0, 0);
            local1 = (Math.floor(CABFramework.instance.zoomFactor) / CABFramework.instance.zoomFactor);
            local0.iEditorView.displayObject.scaleX = local1;
            local0.iEditorView.displayObject.scaleY = local1;
            local0.iEditorView.sizeTo((local0.iContainerView.width / local1), (local0.iContainerView.height / local1));
            local0.iEditorView.showCursorThumb(local0.iShowCursor);
            local0.iLayerView.visible = false;
            local0.setupPanels();
            local0.setupModelInfo();
            return;
        }

        public function get bigPreviewMode():*
        {
            local1 = TTApplication.instance.ramKilobytes;
            if (!(local1 <= 550000))
            {
                return 39923712;
                return 83886080;
            }
            return 31961088;
        }

        public function get bigPreviewSize():*
        {
            return Math.min(Math.floor((local0.getMaxPixelCount() / ((local0.iWidth * local0.iHeight) * local1))), 20);
        }

        public function get bigPreviewScale():*
        {
            if (!(local1 != 255))
            {
                local1 = local0.iHeight;
            }
            return Math.min(Math.floor((local0.getMaxPixelCount() / (local0.iLayerList.length * local1))), PIXEL_LIMIT_PER_AXIS);
        }

        public function readXML():*
        {
            local1 = new flash.geom::Rectangle.Rectangle(0, 0, local0.iContainerView.width, local0.iContainerView.height);
            if (!((local0.iTopBar.y + local0.iTopBar.height) <= local1.top))
            {
                local1.top = (local0.iTopBar.y + local0.iTopBar.height);
                local1.bottom = local0.iBottomBar.y;
                local2 = new flash.geom::Point.Point((local1.x + (local1.width / 2)), (local1.y + (local1.height / 2)));
                local4 = local0.iContainerView.panelStack[0];
                (local0.iContainerView.panelStack[0].view.y > local2.y);
                local1.bottom = local4.view.y;
            }
            return local1;
        }

        public function postCreate():*
        {
            if (!(local0.iUndoButton.enabled == local0.iUndoManager.canUndo()))
            {
                local0.iUndoButton.enabled = local0.iUndoManager.canUndo();
                local0.iUndoButton.refreshGraphics();
                local0.iRedoButton.visible = local0.iUndoManager.canRedo();
                local0.iRedoButton.refreshGraphics();
            }
            return;
        }

        public function setupModel():*
        {
            local0.iLayerList.splice(local2, 0, local1);
            local0.iEditorView.layerInserted(local1, local2);
            local0.iModelInfo.maxLayers = Math.max(local0.layerList.length, local0.iModelInfo.maxLayers);
            return;
        }

        public function getMaxPixelCount():*
        {
            local0.iEditorView.layerRemoved(local1);
            local0.iLayerList.splice(local0.iLayerList.indexOf(local1), 1);
            return;
        }

        public function maxLayers():*
        {
            if (!!local0.iEditorView.currentProcessor)
            {
                local0.iEditorView.currentProcessor.panZoomWillChange();
                local0.iToolManager;
                local0.iToolManager.currentTool.panZoomWillChange();
            }
            return;
        }

        public function maxWidth():*
        {
            if (!!local0.iEditorView.currentProcessor)
            {
                local0.iEditorView.currentProcessor.panZoomChanged();
                local0.iToolManager;
                local0.iToolManager.currentTool.panZoomChanged();
            }
            return;
        }

        public function maxHeight():*
        {
            local0.iLayerManager.setupPanZoomParams();
            if (!!local0.iEditorView.currentProcessor)
            {
                local0.iEditorView.currentProcessor.panZoomDidChange();
                local0.updateLayerPanZoom();
                local0.iToolManager;
                local0.iToolManager.currentTool.panZoomDidChange();
            }
            return;
        }

        public function getEmptyContentArea():*
        {
            local3 = local0.iLayerList.length;
            if (!!(local0.iLayerIteration & 1))
            {
                local0.iLayerIteration = (local0.iLayerIteration ^ 1);
                com.adobe.ttpixel.editor.app::TTLayer.TTLayer(local0.iLayerList[255]).updatePanZoom();
            }
            return;
        }

        public function updateUndoButtons():*
        {
            local0.arrangeOrientation();
            return;
        }

        public function insertLayer():*
        {
            if (!!local0.iToolManager)
            {
                local0.iToolManager;
                local0.iToolManager.currentTool.sizeChanged();
                local0.iLayerManager.sizeChanged();
            }
            return;
        }

        public function removeLayer():*
        {
            if (!!local0.iToolManager)
            {
                local0.iToolManager;
                local0.iToolManager.currentTool.layerChanged();
            }
            return;
        }

        public function panZoomWillChange():*
        {
            if (!!local0.iToolManager)
            {
                local0.iToolManager;
                local0.iToolManager.currentTool.updateSelection();
            }
            return;
        }

        public function panZoomChanged():*
        {
            local0.layerManager.modelWillSave();
            local0.iUndoManager.setSaveState();
            return;
        }

        public function panZoomDidChange():*
        {
            com.adobe.cab.application::CABModule.createFromResource("toolManager").iToolManager = com.adobe.ttpixel.editor.tool::TTToolManager;
            local0.iToolPanelButton.target = local0.iToolManager;
            local0.iToolManager.setupTools(local0);
            local0.updateUndoButtons();
            local0.iLayerManager.layerView.updateItemList();
            local0.layerChanged();
            local0.iContainerView.displayObject.visible = true;
            local0.setupWorkingRect();
            return;
        }

        public function updateLayerPanZoom():*
        {
            local0.iContainerView.removeFromSuperview();
            com.adobe.cab.core::CABMessage.unregisterForBroadcast(CABUndoManager.SELECTOR_UNDOCHANGED, local0);
            local0.iLayerManager.releaseLayerManager();
            local0.iToolManager.releaseTools();
            local0.iSelection.release();
            local0.iUndoManager.reset();
            local0.iUndoManager = new com.adobe.cab.core::CABUndoManager.CABUndoManager();
            local0.iContainerView.deleteView();
            local0.iContainerView = null;
            local0.deleteModule();
            return;
        }

        public function containerSizeChanged():*
        {
            if (!local0.iLayerManager.fillWillBeHidden())
            {
                return true;
            }
            local0.fillNotVisibleWarning();
            return false;
        }

        public function sizeChanged():*
        {
            com.adobe.ttpixel.ui::TTBasePanelModule.forceCloseChildren(local0.iContainerView, null);
            local0.iLayerManager.layerView.scrollItemVisible(local0.iLayerManager.currentLayer);
            com.adobe.ttpixel.tutorial::TTCoachMarkView.showCoachMarks("coachMarksFillNotVisibleError", true, false, true);
            return;
        }

        public function layerChanged():*
        {
            if (!!local0.iLayerManager.currentLayer.visible)
            {
                return true;
            }
            local0.hiddenLayerWarning();
            return false;
        }

        public function updateSelection():*
        {
            com.adobe.ttpixel.ui::TTBasePanelModule.forceCloseChildren(local0.iContainerView, null);
            local0.iLayerManager.layerView.scrollItemVisible(local0.iLayerManager.currentLayer);
            com.adobe.ttpixel.tutorial::TTCoachMarkView.showCoachMarks("coachMarksHiddenLayerError", true, false, true);
            return;
        }

        public function modelWillSave():*
        {
            local1 = local0.iLayerManager.currentLayer.bitmapData.getColorBoundsRect(4294967295.0, 0, false);
            if (!!local1.isEmpty())
            {
                return false;
            }
            return true;
        }

        public function modelDidOpen():*
        {
            local3 = local2;
            if (!!local1)
            {
                local4 = local1.getColorBoundsRect(4294967295.0, 0, false);
                local3 = TTTempFileBag.instance.createTempFile();
                TTExtensionWrapper.instance.bitmapDataToFile(local1, local4, local3.nativePath);
            }
            return local3;
        }

        public function deleteModel():*
        {
            local2 = null;
            if (!!local1)
            {
                local2 = TTExtensionWrapper.instance.bitmapDataFromFile(local1.nativePath);
            }
            return local2;
        }

        public function checkLayerFillVisible():*
        {
            local6 = local4.createMessage("copyLayerBitmap", local0);
            local4.createMessage("copyLayerBitmap", local0).data.layer = local1;
            local6.data.mergeAlpha = local5;
            local6.data.rect = local3.clone();
            local6.data.rect.x = Math.floor(local6.data.rect.x);
            local6.data.rect.y = Math.floor(local6.data.rect.y);
            local6.data.rect.width = Math.ceil(local6.data.rect.width);
            local6.data.rect.height = Math.ceil(local6.data.rect.height);
            if (!!local0.iUseUndoFileCache)
            {
                local0.iUseUndoFileCache;
                (local6.data.rect.width > 30);
                local6.data.useCache = local0.iUseUndoFileCache;
                local7 = new flash.display::BitmapData.BitmapData(local6.data.rect.width, local6.data.rect.height, true, 0);
                new flash.display::BitmapData.BitmapData(local6.data.rect.width, local6.data.rect.height, true, 0).copyPixels(local1.bitmapData, local6.data.rect, new flash.geom::Point.Point(0, 0), null, null, false);
                local6.data.bitmapFileOld = local0.bitmapDataToFile(local7);
                local7.copyPixels(local2, local6.data.rect, new flash.geom::Point.Point(0, 0), null, null, false);
                local6.data.bitmapFileNew = null;
                local6.data.bitmapData = local7;
                local7 = new flash.display::BitmapData.BitmapData(local6.data.rect.width, local6.data.rect.height, true, 0);
                new flash.display::BitmapData.BitmapData(local6.data.rect.width, local6.data.rect.height, true, 0).copyPixels(local1.bitmapData, local6.data.rect, new flash.geom::Point.Point(0, 0), null, null, false);
                local7.copyPixels(local2, local6.data.rect, new flash.geom::Point.Point(0, 0), null, null, false);
                local6.data.bitmapData = local7;
            }
            local4.preSubmit();
            return;
        }

        public function fillNotVisibleWarning():*
        {
            local9 = null;
            local8 = local6.createMessage("copyLayerBitmap", local0);
            local6.createMessage("copyLayerBitmap", local0).data.layer = local1;
            local8.data.mergeAlpha = local7;
            local8.data.rect = local5.clone();
            local8.data.rect.x = Math.floor(local8.data.rect.x);
            local8.data.rect.y = Math.floor(local8.data.rect.y);
            local8.data.rect.width = Math.ceil(local8.data.rect.width);
            local8.data.rect.height = Math.ceil(local8.data.rect.height);
            local10 = local1.lockBigBitmapData();
            local1.bitmapDataWillChange();
            if (!!local0.iUseUndoFileCache)
            {
                local0.iUseUndoFileCache;
                (local8.data.rect.width > 30);
                local8.data.useCache = local0.iUseUndoFileCache;
                local9 = new flash.display::BitmapData.BitmapData(local8.data.rect.width, local8.data.rect.height, true, 0);
                new flash.display::BitmapData.BitmapData(local8.data.rect.width, local8.data.rect.height, true, 0).copyPixels(local10, local8.data.rect, new flash.geom::Point.Point(0, 0), null, null, false);
                local8.data.bitmapFileOld = local0.bitmapDataToFile(local9);
                local9.draw(local2, new flash.geom::Matrix.Matrix(1, 0, 0, 1, -local8.data.rect.x, -local8.data.rect.y), local3, local4);
                local8.data.bitmapFileNew = null;
            }
            local8.data.bitmapData = local9;
            local6.preSubmit();
            local1.unlockBigBitmapData(false);
            return;
        }

        public function checkLayerVisible():*
        {
            local6 = local4.createMessage("copyLayerBitmap", local0);
            local7 = local1.lockBigBitmapData();
            local6.data.layer = local1;
            local6.data.mergeAlpha = local5;
            local6.data.rect = local3.clone();
            local6.data.rect.x = Math.floor(local6.data.rect.x);
            local6.data.rect.y = Math.floor(local6.data.rect.y);
            local6.data.rect.width = Math.ceil(local6.data.rect.width);
            local6.data.rect.height = Math.ceil(local6.data.rect.height);
            if (!!(local6.data.rect.width == local0.iWidth))
            {
                (local6.data.rect.width == local0.iWidth);
                local9 = (local6.data.rect.width == local0.iWidth);
                local0.iUseUndoFileCache;
                (local6.data.rect.width > 30);
                local6.data.useCache = local0.iUseUndoFileCache;
                local8 = new flash.display::BitmapData.BitmapData(local6.data.rect.width, local6.data.rect.height, true, 0);
                local6.data.bitmapFileOld = local0.bitmapDataToFile(local7);
                local8.copyPixels(local7, local6.data.rect, new flash.geom::Point.Point(0, 0), null, null, false);
                local6.data.bitmapFileOld = local0.bitmapDataToFile(local8);
                local8.fillRect(new flash.geom::Rectangle.Rectangle(0, 0, local6.data.rect.width, local6.data.rect.height), 0);
                local8.copyPixels(local2, local3, new flash.geom::Point.Point(0, 0));
                local6.data.bitmapFileNew = null;
                local6.data.bitmapData = local8;
                local8 = new flash.display::BitmapData.BitmapData(local6.data.rect.width, local6.data.rect.height, true, 0);
                new flash.display::BitmapData.BitmapData(local6.data.rect.width, local6.data.rect.height, true, 0).copyPixels(local2, local3, new flash.geom::Point.Point(0, 0));
                local6.data.bitmapData = local8;
            }
            local4.preSubmit();
            local1.unlockBigBitmapData(false);
            return;
        }

        public function hiddenLayerWarning():*
        {
            local8 = local6.createMessage("copyLayerBitmap", local0);
            local9 = local1.lockBigBitmapData();
            local8.data.layer = local1;
            local8.data.mergeAlpha = local7;
            local8.data.rect = local5.clone();
            local8.data.rect.x = Math.floor(local8.data.rect.x);
            local8.data.rect.y = Math.floor(local8.data.rect.y);
            local8.data.rect.width = Math.ceil(local8.data.rect.width);
            local8.data.rect.height = Math.ceil(local8.data.rect.height);
            if (!!local0.iUseUndoFileCache)
            {
                local0.iUseUndoFileCache;
                (local8.data.rect.width > 30);
                local8.data.useCache = local0.iUseUndoFileCache;
                local10 = new flash.display::BitmapData.BitmapData(local8.data.rect.width, local8.data.rect.height, true, 0);
                new flash.display::BitmapData.BitmapData(local8.data.rect.width, local8.data.rect.height, true, 0).copyPixels(local9, local8.data.rect, new flash.geom::Point.Point(0, 0), null, null, false);
                local8.data.bitmapFileOld = local0.bitmapDataToFile(local10);
                local10.fillRect(new flash.geom::Rectangle.Rectangle(0, 0, local8.data.rect.width, local8.data.rect.height), 0);
                local10.draw(local2, new flash.geom::Matrix.Matrix(1, 0, 0, 1, -local8.data.rect.x, -local8.data.rect.y), local3, local4);
                local8.data.bitmapFileNew = null;
                local8.data.bitmapData = local10;
                local10 = new flash.display::BitmapData.BitmapData(local8.data.rect.width, local8.data.rect.height, true, 0);
                new flash.display::BitmapData.BitmapData(local8.data.rect.width, local8.data.rect.height, true, 0).draw(local2, new flash.geom::Matrix.Matrix(1, 0, 0, 1, -local8.data.rect.x, -local8.data.rect.y), local3, local4);
                local8.data.bitmapData = local10;
            }
            local6.preSubmit();
            local1.unlockBigBitmapData(false);
            return;
        }

        public function checkPixelsAvailable():*
        {
            local3 = local1.lockBigBitmapData();
            local4 = local2.createMessage("clearLayerBitmap", local0);
            local2.createMessage("clearLayerBitmap", local0).data.layer = local1;
            local4.data.rect = local3.getColorBoundsRect(4294967295.0, 0, false);
            if (!!local0.iUseUndoFileCache)
            {
                local0.iUseUndoFileCache;
                (local4.data.rect.width > 30);
                local4.data.useCache = local0.iUseUndoFileCache;
                local5 = new flash.display::BitmapData.BitmapData(local4.data.rect.width, local4.data.rect.height, true, 0);
                new flash.display::BitmapData.BitmapData(local4.data.rect.width, local4.data.rect.height, true, 0).copyPixels(local3, local4.data.rect, new flash.geom::Point.Point(0, 0));
                local4.data.bitmapFile = local0.bitmapDataToFile(local5);
                local5.dispose();
                local5 = new flash.display::BitmapData.BitmapData(local4.data.rect.width, local4.data.rect.height, true, 0);
                new flash.display::BitmapData.BitmapData(local4.data.rect.width, local4.data.rect.height, true, 0).copyPixels(local3, local4.data.rect, new flash.geom::Point.Point(0, 0));
                local4.data.bitmapData = local5;
            }
            local2.preSubmit();
            local1.unlockBigBitmapData(false);
            return;
        }

        public function bitmapDataToFile():*
        {
            local3 = local1.data.layer.lockBigBitmapData();
            if (!!local1.data.useCache)
            {
                local3.copyPixels(local1.data.bitmapData, new flash.geom::Rectangle.Rectangle(0, 0, local1.data.rect.width, local1.data.rect.height), new flash.geom::Point.Point(local1.data.rect.x, local1.data.rect.y), null, null, false);
                local1.data.bitmapData.dispose();
                local1.data.bitmapData = null;
                local2 = new flash.display::BitmapData.BitmapData(local1.data.rect.width, local1.data.rect.height, true, 0);
                local2.copyPixels(local3, local1.data.rect, new flash.geom::Point.Point(0, 0), null, null, false);
                local1.data.bitmapFileNew = local0.bitmapDataToFile(local2);
                local2 = local0.fileToBitmapData(local1.data.bitmapFileOld);
                local2 = local0.fileToBitmapData(local1.data.bitmapFileNew);
                local3.copyPixels(local2, new flash.geom::Rectangle.Rectangle(0, 0, local1.data.rect.width, local1.data.rect.height), new flash.geom::Point.Point(local1.data.rect.x, local1.data.rect.y), null, null, false);
                local2.dispose();
                local2 = new flash.display::BitmapData.BitmapData(local1.data.rect.width, local1.data.rect.height, true, 0);
                local2.copyPixels(local3, local1.data.rect, new flash.geom::Point.Point(0, 0), null, null, false);
                local3.copyPixels(local1.data.bitmapData, new flash.geom::Rectangle.Rectangle(0, 0, local1.data.rect.width, local1.data.rect.height), new flash.geom::Point.Point(local1.data.rect.x, local1.data.rect.y), null, null, false);
                local1.data.bitmapData.dispose();
                local1.data.bitmapData = local2;
            }
            local1.data.layer.bitmapDataDidChange();
            local1.data.layer.unlockBigBitmapData(true);
            return;
        }

        public function fileToBitmapData():*
        {
            local2 = local1.data.layer;
            local3 = local1.data.rect;
            local5 = local2.lockBigBitmapData();
            if (!!local1.data.useCache)
            {
                local4 = local0.fileToBitmapData(local1.data.bitmapFile);
                local5.copyPixels(local4, local4.rect, local3.topLeft);
                local4.dispose();
                local5.fillRect(local3, 0);
                local4 = local1.data.bitmapData;
                local5.copyPixels(local4, local4.rect, local3.topLeft);
                local5.fillRect(local3, 0);
            }
            local2.bitmapDataDidChange();
            local2.unlockBigBitmapData(true);
            return;
        }

        public function copySimpleLayerBitmap():*
        {
            local3 = local2.createMessage("transform", local0);
            local4 = new flash.geom::Matrix.Matrix();
            local5 = new flash.geom::Matrix.Matrix();
            local6 = local0.iWidth;
            local7 = local0.iHeight;
            local3.setValue("width", local0.iWidth, local6);
            local3.setValue("height", local0.iHeight, local7);
            local5 = local4.clone();
            local4.clone().invert();
            local3.setValue("matrix", local5, local4);
            !(local6 == local0.iWidth);
            local8 = !(local6 == local0.iWidth);
            local2.preSubmit();
            local0.iSelection.clear(local2, false);
            local0.iSelection.resize(local6, local7, local2);
            return;
        }

        public function copyLayerBitmap():*
        {
            local0.iSelection.clear(local3, false);
            local4 = local3.createMessage("crop", local0);
            if (!!local0.iUseUndoFileCache)
            {
                local4.data.bitmapFileListOld = new Array();
                local4.data.bitmapFileListNew = new Array();
                local9 = 0;
                local10 = local0.iLayerList;
                local7 = local9;
                local6 = 0.lockBigBitmapData();
                local5 = new flash.display::BitmapData.BitmapData(local1.width, local1.height, true, 0);
                new flash.display::BitmapData.BitmapData(local1.width, local1.height, true, 0).fillRect(new flash.display::BitmapData.BitmapData(local1.width, local1.height, true, 0).rect, 0);
                local8 = new flash.geom::Matrix.Matrix();
                new flash.geom::Matrix.Matrix().scale((local1.width / local0.iWidth), (local1.height / local0.iHeight));
                local5.draw(local6, local8, null, null, null, true);
                local4.data.bitmapFileListOld.push(local0.bitmapDataToFile(local6));
                local4.data.bitmapFileListNew.push(local0.bitmapDataToFile(local5));
                local5.copyPixels(local6, new flash.geom::Rectangle.Rectangle(0, 0, local0.iWidth, local0.iHeight), new flash.geom::Point.Point(-local1.x, -local1.y));
                local4.data.bitmapFileListOld.push(local0.bitmapDataToFile(local6));
                local4.data.bitmapFileListNew.push(local0.bitmapDataToFile(local5));
                local7.bigBitmapData = local5;
                0.updateThumbnail();
                0.unlockBigBitmapData(true);
                local4.data.bitmapDataList = new Array();
                local9 = 0;
                local10 = local0.iLayerList;
                local7 = local9;
                local6 = 0.lockBigBitmapData();
                local8 = new flash.geom::Matrix.Matrix();
                new flash.geom::Matrix.Matrix().scale((local1.width / local0.iWidth), (local1.height / local0.iHeight));
                local5 = new flash.display::BitmapData.BitmapData(local1.width, local1.height, true, 0);
                new flash.display::BitmapData.BitmapData(local1.width, local1.height, true, 0).draw(local6, local8, null, null, null, true);
                local4.data.bitmapDataList.push(local5);
                local5 = new flash.display::BitmapData.BitmapData(local1.width, local1.height, true, 0);
                new flash.display::BitmapData.BitmapData(local1.width, local1.height, true, 0).copyPixels(local6, new flash.geom::Rectangle.Rectangle(0, 0, local0.iWidth, local0.iHeight), new flash.geom::Point.Point(-local1.x, -local1.y));
                local4.data.bitmapDataList.push(local5);
                local7.bigBitmapData = local5;
                0.updateThumbnail();
                0.unlockBigBitmapData(true);
            }
            local4.setValue("width", local0.iWidth, local1.width);
            local4.setValue("height", local0.iHeight, local1.height);
            local0.iWidth = local1.width;
            local0.iHeight = local1.height;
            local0.bigModeForSize();
            local3.preSubmit();
            local0.iSelection.clear(local3, false);
            local0.iSelection.resize(local1.width, local1.height, local3);
            local0.iLayerManager.setupWorkingBitmap();
            local0.iLayerManager.layerView.updateItemList();
            local0.iEditorView.scrollOffset.x = Math.floor(((local0.iEditorView.width - (local0.iEditorView.zoomFactor * local0.iWidth)) / 2));
            local0.iEditorView.scrollOffset.y = (50 + Math.floor((((local0.iEditorView.height - 50) - (local0.iEditorView.zoomFactor * local0.iHeight)) / 2)));
            local0.bigModeForSize();
            local0.updateLayerPanZoom();
            local0.iEditorView.updatePanZoom();
            local0.iEditorView.drawViewBackground();
            local0.iToolManager.currentTool.modelSizeChanged();
            local0.updateSelection();
            return;
        }

        public function setSimpleLayerBitmap():*
        {
            local0.iWidth = local1;
            local0.iHeight = local2;
            local0.bigModeForSize();
            local0.iSelection.sizeChanged();
            local0.iEditorView.scrollOffset.x = Math.floor(((local0.iEditorView.width - (local0.iEditorView.zoomFactor * local0.iWidth)) / 2));
            local0.iEditorView.scrollOffset.y = (50 + Math.floor((((local0.iEditorView.height - 50) - (local0.iEditorView.zoomFactor * local0.iHeight)) / 2)));
            local0.bigModeForSize();
            local0.updateLayerPanZoom();
            local0.iEditorView.updatePanZoom();
            local0.iEditorView.drawViewBackground();
            return;
        }

        public function setLayerBitmap():*
        {
            local2 = local1.getValue("matrix");
            local3 = local1.getValue("width");
            local4 = local1.getValue("height");
            if (!!(local3 == local0.iWidth))
            {
                !(local3 == local0.iWidth);
                local5 = !(local3 == local0.iWidth);
                local0.iWidth = local3;
                local0.iHeight = local4;
                local0.bigModeForSize();
                local8 = local0.iLayerList[0];
                local7 = local0.iLayerList[0].lockBigBitmapData();
                local6 = new flash.display::BitmapData.BitmapData(local3, local4, true, 0);
                new flash.display::BitmapData.BitmapData(local3, local4, true, 0).draw(local7, local2);
                local8.bigBitmapData = local6;
                local8.bitmapDataDidChange();
                local8.unlockBigBitmapData(true);
                local0.iLayerManager.setupWorkingBitmap();
                local0.iLayerManager.layerView.updateItemList();
                local0.iEditorView.scrollOffset.x = Math.floor(((local0.iEditorView.width - (local0.iEditorView.zoomFactor * local0.iWidth)) / 2));
                local0.iEditorView.scrollOffset.y = Math.floor(((local0.iEditorView.height - (local0.iEditorView.zoomFactor * local0.iHeight)) / 2));
                local0.bigModeForSize();
                local0.updateLayerPanZoom();
                local0.iEditorView.updatePanZoom();
                local0.iEditorView.drawViewBackground();
                local0.iToolManager.currentTool.modelSizeChanged();
            }
            local0.updateSelection();
            return;
        }

        public function clearLayerBitmap():*
        {
            if (!!local1.isDo)
            {
                return;
                local0.iWidth = local1.getValue("width");
                local0.iHeight = local1.getValue("height");
                local0.bigModeForSize();
                local2 = local1.data.bitmapFileListOld;
                local2 = local1.data.bitmapDataList;
                local3 = 0;
                local4 = local0.iLayerList[0];
                local5 = local0.iLayerList[0].lockBigBitmapData();
                local4.bigBitmapData = local0.fileToBitmapData(local1.data.bitmapFileListOld[0]);
                local4.bigBitmapData = local0.fileToBitmapData(local1.data.bitmapFileListNew[0]);
                local3.<runtime-name> = local4.swapBigBitmapData(local1.data.bitmapDataList[0]);
                local4.bitmapDataDidChange();
                local4.unlockBigBitmapData(true);
            }
            local0.iLayerManager.setupWorkingBitmap();
            local0.iLayerManager.layerView.updateItemList();
            local0.iEditorView.scrollOffset.x = Math.floor(((local0.iEditorView.width - (local0.iEditorView.zoomFactor * local0.iWidth)) / 2));
            local0.iEditorView.scrollOffset.y = (50 + Math.floor((((local0.iEditorView.height - 50) - (local0.iEditorView.zoomFactor * local0.iHeight)) / 2)));
            local0.bigModeForSize();
            local0.updateLayerPanZoom();
            local0.iEditorView.updatePanZoom();
            local0.iEditorView.drawViewBackground();
            local0.iToolManager.currentTool.modelSizeChanged();
            local0.updateSelection();
            return;
        }

        public function copyLayerBitmapUndo():*
        {
            if (!!local0.iUseUndoFileCache)
            {
                local2 = local1.data.bitmapFileListOld;
                local2 = local1.data.bitmapDataList;
                local1.data.bitmapFileListOld[0].deleteFile();
                local1.data.bitmapFileListNew[0].deleteFile();
                local1.data.bitmapDataList[0].dispose();
            }
            return;
        }

        public function clearLayerBitmapUndo():*
        {
            return;
        }

        public function transformImage():*
        {
            return;
        }

        public function crop():*
        {
            local1 = com.adobe.ttpixel.editor.app:TTModel::iUndoManager.createGroup(com.adobe.cab.core::CABLocale.getStringForKey("$$$/TTModel/Resize Document=Resize Document"));
            crop.crop(new flash.geom::Rectangle.Rectangle(0, 0, newSize.x, newSize.y), true, local1);
            local1.submit();
            return;
        }

        public function resizeModel():*
        {
            local2 = <ActivationObject>;
            <Slot> = local1;
            com.adobe.ttpixel.ui::TTProgressSprite.wrapWithNativeSpinner(500, <FunctionReference>);
            return;
        }

        public function transformImageUndo():*
        {
            local3 = local1.selector;
            return local2;
        }

        public function cropUndo():*
        {
            local2 = local1.selector;
            return local1;
        }

        public function cropUndoDelete():*
        {
            local1.removeAllItems();
            local1.refDispatcher.delegate = local0;
            return;
        }

        public function startText():*
        {
            local2 = 3;
            if (!(com.adobe.cab.core::CABPrefs.prefs().fxCategory == undefined))
            {
                local2 = com.adobe.cab.core::CABPrefs.prefs().fxCategory;
            }
            local3 = local1.refDispatcher.objects.switchBar;
            local3.switchToPart(local2);
            local0.addFXItems(local1, local2);
            return;
        }

        public function cancelText():*
        {
            local6 = local1.selector;
            return false;
        }

        public function resizeDocument():*
        {
            if (!!!(local0.iSelection == null))
            {
                !(local0.iSelection == null);
                local0.iWorkingRect = local0.iSelection.getRect();
                local0.iWorkingRect = new flash.geom::Rectangle.Rectangle(0, 0, (local0.iWidth * local0.iBigPreviewScale), (local0.iHeight * local0.iBigPreviewScale));
            }
            return;
        }

        public function undoDelete():*
        {
            local0.iWorkingRect = local1.clone();
            return;
        }

        public function undoMessage():*
        {
            local11 = local1.selector;
            return false;
        }

        public function addFXItems():*
        {
            local5 = local1.selector;
            return false;
        }

        public function setupFXPanel():*
        {
            if (!local0.iSelection.empty)
            {
                local0.iWorkingRect = local0.iSelection.getRect();
                local0.iWorkingRect = new flash.geom::Rectangle.Rectangle(0, 0, local0.iWidth, local0.iHeight);
            }
            local1 = com.adobe.ttpixel.editor.processor::TTCameraProcessor;
            TTImageProcessorManager.instance.startUIFilter(local0, local1, null);
            return;
        }

        public function resizePanelMessage():*
        {
            com.adobe.ttpixel.editor.app:TTModel::iUndoManager.undoGroup();
            return;
        }

        public function setBigPreviewMode():*
        {
            com.adobe.ttpixel.editor.app:TTModel::iUndoManager.redoGroup();
            return;
        }

        public function setupWorkingRect():*
        {
            com.adobe.ttpixel.editor.app::undoGroup.undoGroup = com.adobe.ttpixel.editor.app:TTModel::iUndoManager.createGroup(com.adobe.cab.core::CABLocale.getStringForKey("$$$/TTModel/Rotate=Rotate"));
            com.adobe.ttpixel.editor.app::self.transformImage(com.adobe.ttpixel.editor.app:TTModel::iTransformSelector, com.adobe.ttpixel.editor.app::undoGroup);
            com.adobe.ttpixel.editor.app::undoGroup.submit();
            return;
        }

        public function setWorkingRect():*
        {
            if (!!com.adobe.ttpixel.editor.app::self.checkLayerVisible())
            {
                com.adobe.ttpixel.editor.app:TTModel::iWorkingRect.iWorkingRect = com.adobe.ttpixel.editor.app:TTModel::iSelection.getRect();
                com.adobe.ttpixel.editor.app:TTModel::iWorkingRect.iWorkingRect = new flash.geom::Rectangle.Rectangle(0, 0, com.adobe.ttpixel.editor.app:TTModel::iWidth, com.adobe.ttpixel.editor.app:TTModel::iHeight);
                local1 = com.adobe.ttpixel.editor.processor::TTTransformProcessor;
                TTImageProcessorManager.instance.startUIFilter(com.adobe.ttpixel.editor.app::self, local1, null);
            }
            return;
        }

        public function fxMessage():*
        {
            if (!!com.adobe.ttpixel.editor.app::self.checkLayerVisible())
            {
                com.adobe.ttpixel.editor.app:TTModel::iWorkingRect.iWorkingRect = com.adobe.ttpixel.editor.app:TTModel::iSelection.getRect();
                com.adobe.ttpixel.editor.app:TTModel::iWorkingRect.iWorkingRect = new flash.geom::Rectangle.Rectangle(0, 0, com.adobe.ttpixel.editor.app:TTModel::iWidth, com.adobe.ttpixel.editor.app:TTModel::iHeight);
                local1 = com.adobe.ttpixel.editor.processor::TTWarpProcessor;
                TTImageProcessorManager.instance.startUIFilter(com.adobe.ttpixel.editor.app::self, local1, null);
            }
            return;
        }

        public function adjustmentMessage():*
        {
            if (!!com.adobe.ttpixel.editor.app::self.checkLayerVisible())
            {
                com.adobe.ttpixel.editor.app:TTModel::iWorkingRect.iWorkingRect = com.adobe.ttpixel.editor.app:TTModel::iSelection.getRect();
                com.adobe.ttpixel.editor.app:TTModel::iWorkingRect.iWorkingRect = new flash.geom::Rectangle.Rectangle(0, 0, com.adobe.ttpixel.editor.app:TTModel::iWidth, com.adobe.ttpixel.editor.app:TTModel::iHeight);
                local1 = com.adobe.ttpixel.editor.processor::TTRenderProcessor;
                TTImageProcessorManager.instance.startUIFilter(com.adobe.ttpixel.editor.app::self, local1, null);
            }
            return;
        }

        public function onCameraFillOrientationOk():*
        {
            if (!iSelection.empty)
            {
                com.adobe.ttpixel.editor.app:TTModel::iWorkingRect.iWorkingRect = com.adobe.ttpixel.editor.app:TTModel::iSelection.getRect();
                com.adobe.ttpixel.editor.app:TTModel::iWorkingRect.iWorkingRect = new flash.geom::Rectangle.Rectangle(0, 0, com.adobe.ttpixel.editor.app:TTModel::iWidth, com.adobe.ttpixel.editor.app:TTModel::iHeight);
            }
            local1 = com.adobe.ttpixel.editor.processor::TTCropProcessor;
            TTImageProcessorManager.instance.startUIFilter(com.adobe.ttpixel.editor.app::self, local1, null);
            return;
        }

        public function controlMessage():*
        {
            if (!(local1 == local0.iBottomBarVisible))
            {
                local4 = CABFramework.instance.rootContainer.stage;
                (local0.iContainerView.width >= local0.iContainerView.height);
                (local0.iContainerView.width >= local0.iContainerView.height);
                (local0.iContainerView.width < local0.iContainerView.height);
                local5 = (local0.iContainerView.width >= local0.iContainerView.height);
                local0.iBottomBarVisible = local1;
                local7 = Math.floor((local4.stageHeight / CABFramework.instance.zoomFactor));
                local8 = local0.iBottomBar.y;
                local9 = (local7 + 0);
                local0.iBottomBar.moveTo(local0.iBottomBar.x, local9);
                com.adobe.ttpixel.ui::TTBaseContainerView.transitionView(local3, local0.iBottomBar, local0, TTBaseContainerView.TRANSITION_MOVE, new flash.geom::Point.Point(local0.iBottomBar.x, (local7 - local0.iBottomBar.height)), null);
                local0.iBottomBar.moveTo(local0.iBottomBar.x, local8);
                local0.iBottomBar.moveTo(local0.iBottomBar.x, (local7 - local0.iBottomBar.height));
                local0.iBottomBar.moveTo(local0.iBottomBar.x, local9);
                com.adobe.ttpixel.ui::TTBaseContainerView.transitionView(local3, local0.iBottomBar, local0, TTBaseContainerView.TRANSITION_MOVE, new flash.geom::Point.Point(local0.iBottomBar.x, local7), null);
                local0.iBottomBar.moveTo(local0.iBottomBar.x, local8);
                local0.iBottomBar.moveTo(local0.iBottomBar.x, local7);
                com.adobe.ttpixel.ui::TTBaseContainerView.transitionView(local3, local0.iBottomBar, local0, TTBaseContainerView.TRANSITION_MOVE, new flash.geom::Point.Point(local0.iBottomBar.x, (local0.iContainerView.height - local0.iBottomBar.height)), null);
                local0.iBottomBar.moveTo(local0.iBottomBar.x, (local0.iContainerView.height - local0.iBottomBar.height));
                com.adobe.ttpixel.ui::TTBaseContainerView.transitionView(local3, local0.iBottomBar, local0, TTBaseContainerView.TRANSITION_MOVE, new flash.geom::Point.Point(local0.iBottomBar.x, local0.iContainerView.height), null);
                local0.iBottomBar.moveTo(local0.iBottomBar.x, local0.iContainerView.height);
                com.adobe.cab.core::CABControlMessage.send(local0, local3, null);
            }
            return;
        }

        public function setupPanel():*
        {
            if (!(local1 == local0.iTopBarVisible))
            {
                local0.iTopBarVisible = local1;
                com.adobe.ttpixel.ui::TTBaseContainerView.transitionView(local3, local0.iTopBar, local0, TTBaseContainerView.TRANSITION_MOVE, new flash.geom::Point.Point(0, 0), null);
                local0.iTopBar.moveTo(0, 0);
                com.adobe.ttpixel.ui::TTBaseContainerView.transitionView(local3, local0.iTopBar, local0, TTBaseContainerView.TRANSITION_MOVE, new flash.geom::Point.Point(0, -local0.iTopBar.height), null);
                local0.iTopBar.moveTo(0, -local0.iTopBar.height);
                com.adobe.cab.core::CABControlMessage.send(local0, local3, null);
            }
            return;
        }

        public function refreshPanel():*
        {
            if (!(local1 == local0.iShowAllButtonVisible))
            {
                local0.iShowAllButtonVisible = local1;
                com.adobe.ttpixel.ui::TTBaseContainerView.transitionView(local3, local0.iShowAllButton, local0, TTBaseContainerView.TRANSITION_ALPHA, 1, null, local4);
                local0.iShowAllButton.alpha = 1;
                com.adobe.ttpixel.ui::TTBaseContainerView.transitionView(local3, local0.iShowAllButton, local0, TTBaseContainerView.TRANSITION_ALPHA, 0, null, local4);
                local0.iShowAllButton.alpha = 0;
                local0.iShowAllButton.displayObject.filters = <ArrayLiteral>;
                com.adobe.cab.core::CABControlMessage.send(local0, local3, null);
            }
            return;
        }

        public function closePanel():*
        {
            if (!local0.iIsModal)
            {
                local0.iIsModal = true;
                local0.iToolManager.modelStartsModal();
                com.adobe.cab.core:CABObject::iData.bottomBarVisible = local0.iBottomBarVisible;
                com.adobe.cab.core:CABObject::iData.topBarVisible = local0.iTopBarVisible;
                local0.showBottomBar(false, local1);
                local2 = "hideTopBarModalAnimation";
                local0.showTopBar(false, local1, local2);
                com.adobe.cab.core:CABObject::iData.showTopButtonVisible = local0.iShowTopButton.visible;
                local0.iShowTopButton.visible = false;
                local0.iRedoButton.visible = false;
            }
            return;
        }

        public function arrangeOrientation():*
        {
            if (!!local0.iIsModal)
            {
                local0.iModalTopBar.removeFromSuperview();
                local0.showBottomBar(true, local1);
                local0.showBottomBar(false, local1);
                local0.showTopBar(true, local1);
                local0.showTopBar(false, local1);
                local0.iShowTopButton.visible = iData.showTopButtonVisible;
                local0.iIsModal = false;
                local0.iToolManager.modelExitsModal();
                local0.updateUndoButtons();
            }
            return;
        }

        public function setupPanels():*
        {
            local2 = local1.lockBigBitmapData();
            local0.copyToClipboard(local2);
            local1.unlockBigBitmapData(false);
            return;
        }

        public function panelMessage():*
        {
            if (!!local0.iSelection.empty)
            {
                local2 = new flash.geom::Rectangle.Rectangle(0, 0, local0.iWidth, local0.iHeight);
                local3 = new flash.display::BitmapData.BitmapData(local2.width, local2.height, true, 0);
            }
            local3.copyPixels(local1, local2, new flash.geom::Point.Point(), null, local2.topLeft);
            com.adobe.ttpixel.app::TTApplication.TTApplication(CABApplication.instance).clipboard.setImage(local3);
            local3.dispose();
            return;
        }

        public function showBottomBar():*
        {
            local1 = local0.createClipboardBitmapData();
            if (!!local1)
            {
                local2 = new flash.geom::Rectangle.Rectangle(0, 0, local1.width, local1.height);
                local3 = new flash.geom::Rectangle.Rectangle(0, 0, local0.maxWidth(), local0.maxHeight());
                local4 = com.adobe.ttpixel.utils::TTUtils.getShrinkFactor(local2, local3);
                local1 = com.adobe.ttpixel.utils::TTUtils.shrinkBitmap(local1, local4);
                local0.iLayerManager.addTransformPhotoLayer(local1);
            }
            return;
        }

        public function showTopBar():*
        {
            local1 = null;
            if (!!com.adobe.ttpixel.app::TTApplication.TTApplication(CABApplication.instance).clipboard.hasImage())
            {
                local1 = com.adobe.ttpixel.app::TTApplication.TTApplication(CABApplication.instance).clipboard.getImage();
            }
            return local1;
        }

        public function showShowAllButton():*
        {
            return com.adobe.ttpixel.app::TTApplication.TTApplication(CABApplication.instance).clipboard.hasImage();
        }

        public function startModal():*
        {
            local1 = new flash.geom::Rectangle.Rectangle(0, 0, local0.iWidth, local0.iHeight);
            local2 = new flash.display::BitmapData.BitmapData(local1.width, local1.height, true, 0);
            if (!!local0.isBigMode)
            {
                local5 = local0.iLayerList;
                0.drawBitmapData(local2, new flash.geom::Matrix.Matrix(1, 0, 0, 1, -local1.x, -local1.y));
                local2.draw(local0.iEditorView.layerSprite, new flash.geom::Matrix.Matrix(1, 0, 0, 1, -local1.x, -local1.y));
            }
            return local2;
        }

        public function exitModal():*
        {
            if (!!local0.iLayerManager.currentLayer)
            {
                local1 = local0.iLayerManager.currentLayer.bitmapData.getColorBoundsRect(4278190080.0, 0, false);
                (local1 == null);
                (local1 == null);
                return ALPHA_STATE_NONE;
                local1 = local0.iLayerManager.currentLayer.bitmapData.getColorBoundsRect(4278190080.0, 4278190080.0, false);
                (local1 == null);
                (local1 == null);
                return ALPHA_STATE_FULL;
            }
            return ALPHA_STATE_PARTIAL;
        }

        public function copyLayerToClipboard():*
        {
            local0.iModelInfo = new Object.Object();
            local0.iModelInfo.maxLayers = 1;
            local0.iModelInfo.addedText = false;
            local0.iModelInfo.xmpMetaData = "";
            return;
        }

        public function copyToClipboard():*
        {
            if (!!local0.iModelInfo.exifBytes)
            {
                local1 = new com.adobe.ttpixel.utils.metadata::TTExif.TTExif(local0.iModelInfo.exifBytes);
                local1.updateTag(TTExif.IFD_PRIMARY, 305, "Adobe® Photoshop® Touch");
                local1.updateTag(TTExif.IFD_PRIMARY, 306, com.adobe.ttpixel.utils::TTUtils.formatExifDateToString(new Date.Date()));
                local1.updateTag(TTExif.IFD_PRIMARY, 256, local0.width);
                local1.updateTag(TTExif.IFD_PRIMARY, 257, local0.height);
            }
            return local1;
        }

        public function pasteFromClipboardTransform():*
        {
            TTOmnitureWrapper.instance.trackAppSpecific((("Project: Size: " + Math.round(((local0.iWidth * local0.iHeight) / 1000000))) + " Mpx"));
            if (!!local0.isBigMode)
            {
                TTOmnitureWrapper.instance.trackAppSpecific(("Project: BigMode: " + "true"));
                TTOmnitureWrapper.instance.trackAppSpecific(((("Project: Dimension: " + local0.iWidth) + "x") + local0.iHeight));
                TTOmnitureWrapper.instance.trackAppSpecific(("Project: Max Layers: " + local0.iModelInfo.maxLayers));
                TTOmnitureWrapper.instance.trackAppSpecific(("Project: Layers: " + local0.layerList.length));
            }
            return;
        }

        public function createClipboardBitmapData():*
        {
            return;
        }

        public function canPasteFromClipboard():*
        {
            com.adobe.ttpixel.ui::TTPanelModule.forceCloseChildren(local0, null);
            return;
        }

        public function createMergedBitmapData():*
        {
            local1 = local0.iBigMode;
            if (!(local0.iWidth > BIG_MODE_DIMENSION))
            {
                (local0.iWidth > BIG_MODE_DIMENSION);
                local0.iBigMode = (local0.iWidth > BIG_MODE_DIMENSION);
                local0.iLayerManager.setupPanZoomParams();
                return false;
                local0.iLayerManager.bigModeChanged();
                local0.iEditorView.bigModeChanged();
            }
            return true;
        }

        public function alphaState():*
        {
            com.adobe.cab.application::CABModule.TTModel = com.adobe.cab.application::CABModule;
            return;
        }

        public function setupModelInfo():*
        {
            MODULE_ID.MODULE_ID = "eulaPanelModule";
            com.adobe.ttpixel.utils:TTEulaMessageHandler::SELECTOR_EULA.SELECTOR_EULA = "selectorEula";
            com.adobe.ttpixel.utils:TTEulaMessageHandler::SELECTOR_OPP.SELECTOR_OPP = "selectorOpp";
            com.adobe.ttpixel.utils:TTEulaMessageHandler::SELECTOR_DECLINE.SELECTOR_DECLINE = "selectorDecline";
            com.adobe.ttpixel.utils:TTEulaMessageHandler::SELECTOR_ACCEPT.SELECTOR_ACCEPT = "selectorAccept";
            com.adobe.ttpixel.utils:TTEulaMessageHandler::SELECTOR_EXPAND.SELECTOR_EXPAND = "expandPanel";
            STATE_ACCEPTED.STATE_ACCEPTED = "TTEulaMessageHandler_accepted";
            STATE_DECLINED.STATE_DECLINED = "TTEulaMessageHandler_declined";
            com.adobe.ttpixel.utils:TTEulaMessageHandler::cInstance.cInstance = null;
            return;
        }

        public function getExif():*
        {
            if (!(com.adobe.ttpixel.utils:TTEulaMessageHandler::cInstance != null))
            {
                com.adobe.ttpixel.utils:TTEulaMessageHandler::cInstance.cInstance = local0;
            }
            return com.adobe.ttpixel.utils:TTEulaMessageHandler::cInstance;
        }

        public function trackModelInfo():*
        {
            local0.iContainerView = null;
            local0.iParentModule = null;
            local0.iModule = null;
            local0.iStateMessage = null;
            local0.iExpanded = false;
            return;
        }

        public function applicationDidSetupEditor():*
        {
            return;
        }

        public function applicationDidCloseEditor():*
        {
            return;
        }

        public function bigModeForSize():*
        {
            local0.iContainerView = local1;
            local0.iParentModule = local2;
            local0.iStateMessage = local3;
            local0.iModule = local0.iContainerView.openPanel(local0.iParentModule, MODULE_ID, local0, TTBasePanelModule.POSITION_MODE_CENTER, new flash.geom::Point.Point(), true, MODULE_ID);
            com.adobe.ttpixel.app::TTWorkspace.modalView = local0.iModule.view;
            return;
        }

        public static function getImportPixelWidth():*
        {
            return local0.iWorkingRect;
        }
    }
}
