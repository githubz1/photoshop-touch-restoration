// Decompiled by Phoenix

function method1899()
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
    "return";
}