/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.sample.user.store.manager;


import org.wso2.carbon.user.api.Property;
import org.wso2.carbon.user.core.UserStoreConfigConstants;
import org.wso2.carbon.user.core.jdbc.JDBCRealmConstants;

import java.util.ArrayList;

public class CustomUserStoreConstants {


    //Properties for Read Active Directory User Store Manager
    public static final ArrayList<Property> CUSTOM_UM_MANDATORY_PROPERTIES = new ArrayList<Property>();
    public static final ArrayList<Property> CUSTOM_UM_OPTIONAL_PROPERTIES = new ArrayList<Property>();
    public static final ArrayList<Property> CUSTOM_UM_ADVANCED_PROPERTIES = new ArrayList<Property>();


    static {

        setMandatoryProperty(JDBCRealmConstants.DRIVER_NAME, "", "Full qualified driver name");
        setMandatoryProperty(JDBCRealmConstants.URL, "", "URL of the user store database");
        setMandatoryProperty(JDBCRealmConstants.USER_NAME, "", "Username for the database");
        setMandatoryProperty(JDBCRealmConstants.PASSWORD, "", "Password for the database");

        setProperty(UserStoreConfigConstants.disabled, "false", UserStoreConfigConstants.disabledDescription);
        setProperty("StoreSaltedPassword", "false", "Indicates whether to salt the password");
        setProperty("ReadOnly", "false", "Indicates whether the user store of this realm operates in the user read only mode or not");
        setProperty("PasswordJavaRegEx", "^[a-zA-Z0-9_-]$", "A regular expression to validate passwords");
        setProperty("PasswordJavaScriptRegEx", "^[a-zA-Z0-9_-]$", "The regular expression used by the font-end components for password validation");

        setProperty(UserStoreConfigConstants.SCIMEnabled, "false", UserStoreConfigConstants.SCIMEnabledDescription);


        //Advanced Properties (No descriptions added for each property)
        setAdvancedProperty("AddUserToRoleSQL", "INSERT INTO CUSTOMER_USER_ROLE (CUSTOMER_USER_ID, CUSTOMER_ROLE_ID) VALUES ((SELECT CUSTOMER_ID FROM CUSTOMER_DATA WHERE CUSTOMER_NAME=? ),(SELECT CUSTOMER_ROLE_ID FROM CUSTOMER_ROLE WHERE CUSTOMER_ROLE_NAME=? ))", "");
        setAdvancedProperty("SelectUserSQL", "SELECT * FROM CUSTOMER_DATA WHERE CUSTOMER_NAME=?", "");
        setAdvancedProperty("UserFilterSQL", "SELECT CUSTOMER_NAME FROM CUSTOMER_DATA WHERE CUSTOMER_NAME LIKE ?  ORDER BY CUSTOMER_ID", "");
        setAdvancedProperty("GetRoleListSQL", "SELECT CUSTOMER_ROLE_NAME FROM CUSTOMER_ROLE WHERE CUSTOMER_ROLE_NAME LIKE ?   ORDER BY CUSTOMER_ROLE_NAME", "");
        setAdvancedProperty("UserRoleSQL ", "SELECT CUSTOMER_ROLE_NAME FROM CUSTOMER_USER_ROLE, CUSTOMER_ROLE, CUSTOMER_DATA WHERE CUSTOMER_DATA.CUSTOMER_NAME=? AND CUSTOMER_DATA.CUSTOMER_ID=CUSTOMER_USER_ROLE.CUSTOMER_USER_ID AND CUSTOMER_ROLE.CUSTOMER_ROLE_ID=CUSTOMER_USER_ROLE.CUSTOMER_ROLE_ID", "");
        setAdvancedProperty("GetUserListOfRoleSQL", " SELECT CUSTOMER_NAME   FROM  	 CUSTOMER_USER_ROLE, CUSTOMER_ROLE, CUSTOMER_DATA  WHERE CUSTOMER_ROLE.CUSTOMER_ROLE_NAME= ? AND CUSTOMER_DATA.CUSTOMER_ID=CUSTOMER_USER_ROLE.CUSTOMER_USER_ID  AND CUSTOMER_ROLE.CUSTOMER_ROLE_ID=CUSTOMER_USER_ROLE.CUSTOMER_ROLE_ID", "");
        setAdvancedProperty("AddRoleSQL", "INSERT INTO CUSTOMER_ROLE (CUSTOMER_ROLE_NAME) VALUES (?)", "");
        setAdvancedProperty("UpdateUserPasswordSQL", "UPDATE CUSTOMER_DATA SET PASSWORD=? WHERE CUSTOMER_NAME =?", "");
        setAdvancedProperty("AddUserSQL","INSERT INTO CUSTOMER_DATA (CUSTOMER_NAME, PASSWORD) VALUES(?,?)","");
        setAdvancedProperty("AddRoleToUserSQL", "INSERT INTO CUSTOMER_USER_ROLE (CUSTOMER_ROLE_ID, CUSTOMER_USER_ID) VALUES ((SELECT CUSTOMER_ROLE_ID FROM CUSTOMER_ROLE WHERE CUSTOMER_ROLE_NAME=?),(SELECT CUSTOMER_ID FROM CUSTOMER_DATA WHERE CUSTOMER_NAME=?))", "");

    }


    private static void setProperty(String name, String value, String description) {
        Property property = new Property(name, value, description, (Property[])null);
        CUSTOM_UM_OPTIONAL_PROPERTIES.add(property);

    }



    private static void setMandatoryProperty(String name, String value, String description) {
        Property property = new Property(name, value, description, null);
        CUSTOM_UM_MANDATORY_PROPERTIES.add(property);

    }

    private static void setAdvancedProperty(String name, String value, String description) {
        Property property = new Property(name, value, description, null);
        CUSTOM_UM_ADVANCED_PROPERTIES.add(property);

    }


}
