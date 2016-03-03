package org.wso2.sample.user.store.manager.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.sample.user.store.manager.CustomUserStoreManager;

@Component(name = "demo.CustomUserStore.component",immediate = true)
@Reference(name = "userService",referenceInterface = UserStoreManager.class,
        policy = ReferencePolicy.DYNAMIC,bind = "setUserStore",
        unbind = "unsetUserStore")
public class CustomUserStoreMgtDSComponent {
    private static Log log = LogFactory.getLog(CustomUserStoreMgtDSComponent.class);
    private static UserStoreManager userStoreManager;

    protected void activate(ComponentContext ctxt) {

       /* CustomUserStoreManager customUserStoreManager = new CustomUserStoreManager();
        ctxt.getBundleContext().registerService(UserStoreManager.class.getName(), customUserStoreManager, null);*/
        log.info("CustomUserStoreManager bundle activated successfully..");
    }

    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("Custom User Store Manager is deactivated ");
        }
    }

    protected void setUserStore(UserStoreManager userStore) {
        userStoreManager = userStore;
    }

    protected void unsetUserStore(UserStoreManager userStore) {
        userStoreManager = null;
    }
}
