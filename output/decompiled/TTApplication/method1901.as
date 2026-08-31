// Decompiled by Phoenix

function method1901()
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
    "return";
}
