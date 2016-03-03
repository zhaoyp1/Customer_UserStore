package org.wso2.sample.user.store.manager;

import com.digiwes.wso2test.userstore.UserService;
import org.apache.axiom.om.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.user.api.Properties;
import org.wso2.carbon.user.api.Property;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.common.RoleContext;
import org.wso2.carbon.user.core.jdbc.JDBCRealmConstants;
import org.wso2.carbon.user.core.jdbc.JDBCRoleContext;
import org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager;
import org.wso2.carbon.user.core.profile.ProfileConfigurationManager;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;

import java.sql.*;
import java.util.*;
import java.util.Date;


@Component(name = "userService",immediate = true)
@Service(value = UserStoreManager.class )
public class CustomUserStoreManager extends JDBCUserStoreManager {

    private static Log log = LogFactory.getLog(CustomUserStoreManager.class);
    public CustomUserStoreManager() {
    }

    public CustomUserStoreManager(org.wso2.carbon.user.api.RealmConfiguration realmConfig,
                                  Map<String, Object> properties,
                                  ClaimManager claimManager,
                                  ProfileConfigurationManager profileManager,
                                  UserRealm realm, Integer tenantId)
            throws UserStoreException {
        super(realmConfig, properties, claimManager, profileManager, realm, tenantId, false);
    }

    @Override
    public boolean doAuthenticate(String userName, Object credential) throws UserStoreException {
        Connection dbConnection = null;
        ResultSet rs = null;
        PreparedStatement prepStmt = null;
        String sqlstmt = null;
        String password = (String) credential;
        boolean isAuthed = false;
        try {
            dbConnection = getDBConnection();
            dbConnection.setAutoCommit(false);
            //paring the SELECT_USER_SQL from user_mgt.xml
            sqlstmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.SELECT_USER);
            prepStmt = dbConnection.prepareStatement(sqlstmt);
            prepStmt.setString(1, userName);
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                String storedPassword = rs.getString(2);
                if ((storedPassword != null) && (storedPassword.trim().equals(password))) {
                    isAuthed = true;
                }
            }
        } catch (SQLException e) {
            throw new UserStoreException("Authentication Failure. Using sql :" + sqlstmt);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
        }
        return isAuthed;
    }

    @Override
    public Date getPasswordExpirationTime(String userName) throws UserStoreException {
        return null;
    }
    @Override
    public String[] getUserListOfJDBCRole(RoleContext ctx, String filter) throws UserStoreException {
        System.out.print("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
        String roleName = ctx.getRoleName();
        String[] names = null;
        String sqlStmt = null;

            sqlStmt = this.realmConfig.getUserStoreProperty("GetUserListOfRoleSQL");
            if(sqlStmt == null) {
                throw new UserStoreException("The sql statement for retrieving user roles is null");
            }
            names = this.getStringValuesFromDatabase(sqlStmt, new Object[]{roleName});


        log.debug("Roles are not defined for the role name " + roleName);
        return names;
    }
    @Override
    public String[] doGetUserListOfRole(String roleName, String filter) throws UserStoreException {
        System.out.print("doGetUserListOfRole*************************************************");
        RoleContext roleContext = this.createRoleContext(roleName);
        return getUserListOfJDBCRole(roleContext, filter);
    }

    public String[] doGetExternalRoleListOfUser(String userName, String filter) throws UserStoreException {
        if(log.isDebugEnabled()) {
            log.debug("Getting roles of user: " + userName + " with filter: " + filter);
        }

        String sqlStmt = this.realmConfig.getUserStoreProperty("UserRoleSQL");
        ArrayList roles = new ArrayList();
        if(sqlStmt == null) {
            throw new UserStoreException("The sql statement for retrieving user roles is null");
        } else {
            String[] names;
            names = this.getStringValuesFromDatabase(sqlStmt, new Object[]{userName});


            Collections.addAll(roles, names);
            return (String[])roles.toArray(new String[roles.size()]);
        }
    }
    private String[] getStringValuesFromDatabase(String sqlStmt, Object... params) throws UserStoreException {
        if(log.isDebugEnabled()) {
            log.debug("Execuring Query: " + sqlStmt);

            for(int values = 0; values < params.length; ++values) {
                Object dbConnection = params[values];
                log.debug("Input value: " + dbConnection);
            }
        }

        String[] var13 = new String[0];
        Connection var14 = null;
        Object prepStmt = null;
        Object rs = null;

        try {
            var14 = this.getDBConnection();
            var13 = DatabaseUtil.getStringValuesFromDatabase(var14, sqlStmt, params);
        } catch (SQLException var11) {
            log.error("Using sql : " + sqlStmt);
            throw new UserStoreException(var11.getMessage(), var11);
        } finally {
            DatabaseUtil.closeAllConnections(var14, (ResultSet)rs, new PreparedStatement[]{(PreparedStatement)prepStmt});
        }

        return var13;
    }
    protected boolean isValueExisting(String sqlStmt, Connection dbConnection, Object... params)
            throws UserStoreException {
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        boolean isExisting = false;
        boolean doClose = false;
        try {
            if (dbConnection == null) {
                dbConnection = getDBConnection();
                doClose = true; //because we created it
            }
            if (DatabaseUtil.getStringValuesFromDatabase(dbConnection, sqlStmt, params).length > 0) {
                isExisting = true;
            }
            return isExisting;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            log.error("Using sql : " + sqlStmt);
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            if (doClose) {
                DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
            }
        }
    }

    public String[] getUserListFromProperties(String property, String value, String profileName)
            throws UserStoreException {
        return new String[0];
    }

    @Override
    public String[] doGetRoleNames(String filter, int maxItemLimit) throws UserStoreException {
        String[] roles = new String[0];
        Connection dbConnection = null;
        String sqlStmt = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        if(maxItemLimit == 0) {
            return roles;
        } else {
            try {
                if(filter != null && filter.trim().length() != 0) {
                    filter = filter.trim();
                    filter = filter.replace("*", "%");
                    filter = filter.replace("?", "_");
                } else {
                    filter = "%";
                }

                LinkedList e = new LinkedList();
                dbConnection = this.getDBConnection();
                if(dbConnection == null) {
                    throw new UserStoreException("null connection");
                }

                dbConnection.setAutoCommit(false);
                dbConnection.setTransactionIsolation(2);
                sqlStmt = this.realmConfig.getUserStoreProperty("GetRoleListSQL");
                prepStmt = dbConnection.prepareStatement(sqlStmt);
                byte count = 0;
                byte var19 = (byte)(count + 1);
                prepStmt.setString(var19, filter);
                if(sqlStmt.contains("UM_TENANT_ID")) {
                    ++var19;
                    prepStmt.setInt(var19, this.tenantId);
                }

                this.setPSRestrictions(prepStmt, maxItemLimit);

                try {
                    rs = prepStmt.executeQuery();
                } catch (SQLException var16) {
                    log.error("Error while retrieving roles from JDBC user store", var16);
                }

                if(rs != null) {
                    while(rs.next()) {
                        String name = rs.getString(1);
                        String domain = this.realmConfig.getUserStoreProperty("DomainName");
                        name = UserCoreUtil.addDomainToName(name, domain);
                        e.add(name);
                    }
                }

                if(e.size() > 0) {
                    roles = (String[])e.toArray(new String[e.size()]);
                }
            } catch (SQLException var17) {
                log.error("Using sql : " + sqlStmt);
                throw new UserStoreException(var17.getMessage(), var17);
            } finally {
                DatabaseUtil.closeAllConnections(dbConnection, rs, new PreparedStatement[]{prepStmt});
            }

            return roles;
        }
    }
    private void setPSRestrictions(PreparedStatement ps, int maxItemLimit) throws SQLException {
        boolean givenMax = true;
        boolean searchTime = true;

        int givenMax1;
        try {
            givenMax1 = Integer.parseInt(this.realmConfig.getUserStoreProperty("MaxRoleNameListLength"));
        } catch (Exception var8) {
            givenMax1 = 100;
        }

        int searchTime1;
        try {
            searchTime1 = Integer.parseInt(this.realmConfig.getUserStoreProperty("MaxSearchQueryTime"));
        } catch (Exception var7) {
            searchTime1 = 10000;
        }

        if(maxItemLimit < 0 || maxItemLimit > givenMax1) {
            maxItemLimit = givenMax1;
        }

        ps.setMaxRows(maxItemLimit);

        try {
            ps.setQueryTimeout(searchTime1);
        } catch (Exception var6) {
            log.debug(var6);
        }

    }

    /*@Override
    public Map<String, String> doGetUserClaimValues(String userName, String[] claims,
                                                    String domainName) throws UserStoreException {
        return new HashMap<String, String>();
    }*/

    /*@Override
    public String doGetUserClaimValue(String userName, String claim, String profileName)
            throws UserStoreException {
        return null;
    }*/

    @Override
    public boolean isReadOnly() throws UserStoreException {
        return "true".equalsIgnoreCase(this.realmConfig.getUserStoreProperty("ReadOnly"));
    }

    @Override
    public void doAddUser(String userName, Object credential, String[] roleList,
                          Map<String, String> claims, String profileName,
                          boolean requirePasswordChange) throws UserStoreException {
        /*//UserService userService = new UserService();
        System.out.println("################################################");
        userService.doAddUser(userName, credential, roleList, claims, profileName, requirePasswordChange);
        System.out.println("################################################");*/
       /* Connection dbConnection = null;
        String password = (String)credential;

        try {
            dbConnection = this.getDBConnection();
            String e = this.realmConfig.getUserStoreProperty("AddUserSQL");
            String e1 = null;
            if("true".equalsIgnoreCase((String)this.realmConfig.getUserStoreProperties().get("StoreSaltedPassword"))) {
                byte[] ite = new byte[16];
                this.random.nextBytes(ite);
                e1 = Base64.encode(ite);
            }
            password = this.preparePassword(password, e1);
            this.updateStringValuesToDatabase(dbConnection, e, new Object[]{userName, password, null, Boolean.valueOf(requirePasswordChange), new Date()});
            String propValue;
            dbConnection.commit();
        } catch (Throwable var22) {
            try {
                dbConnection.rollback();
            } catch (SQLException var21) {
                throw new UserStoreException("Error rollbacking add user operation", var21);
            }

            log.error("Error while persisting user : " + userName);
            throw new UserStoreException("Error while persisting user : " + userName, var22);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, new PreparedStatement[0]);
        }
        */
    }

    public void doAddRole(String roleName, String[] userList, boolean shared) throws UserStoreException {
        //UserService userService = new UserService();
        System.out.println("################################################");
        //userService.doAddRole(roleName, userList, shared);


        if(shared && this.isSharedGroupEnabled()) {
            this.doAddSharedRole(roleName, userList);
        }

        Connection dbConnection = null;
        try {
            dbConnection = this.getDBConnection();
            String e = this.realmConfig.getUserStoreProperty("AddRoleSQL");
            if(e.contains("UM_TENANT_ID")) {
                this.updateStringValuesToDatabase(dbConnection, e, new Object[]{roleName, Integer.valueOf(this.tenantId)});
            } else {
                this.updateStringValuesToDatabase(dbConnection, e, new Object[]{roleName});
            }
            dbConnection.commit();
        } catch (SQLException var12) {
            throw new UserStoreException(var12.getMessage(), var12);
        } catch (Exception var13) {
            throw new UserStoreException(var13.getMessage(), var13);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, new PreparedStatement[0]);
        }
        System.out.println("################################################");

    }
    private void updateStringValuesToDatabase(Connection dbConnection, String sqlStmt, Object... params) throws UserStoreException {
        PreparedStatement prepStmt = null;
        boolean localConnection = false;

        try {
            if(dbConnection == null) {
                localConnection = true;
                dbConnection = this.getDBConnection();
            }

            prepStmt = dbConnection.prepareStatement(sqlStmt);
            int e;
            if(params != null && params.length > 0) {
                for(e = 0; e < params.length; ++e) {
                    Object param = params[e];
                    if(param == null) {
                        throw new UserStoreException("Invalid data provided");
                    }

                    if(param instanceof String) {
                        prepStmt.setString(e + 1, (String)param);
                    } else if(param instanceof Integer) {
                        prepStmt.setInt(e + 1, ((Integer)param).intValue());
                    } else if(param instanceof Date) {
                        prepStmt.setTimestamp(e + 1, new Timestamp(System.currentTimeMillis()));
                    } else if(param instanceof Boolean) {
                        prepStmt.setBoolean(e + 1, ((Boolean)param).booleanValue());
                    }
                }
            }

            e = prepStmt.executeUpdate();
            if(log.isDebugEnabled()) {
                if(e == 0) {
                    log.debug("No rows were updated");
                }

                log.debug("Executed querry is " + sqlStmt + " and number of updated rows :: " + e);
            }

            if(localConnection) {
                dbConnection.commit();
            }
        } catch (SQLException var11) {
            log.error("Using sql : " + sqlStmt);
            throw new UserStoreException(var11.getMessage(), var11);
        } finally {
            if(localConnection) {
                DatabaseUtil.closeAllConnections(dbConnection, new PreparedStatement[0]);
            }

            DatabaseUtil.closeAllConnections((Connection)null, new PreparedStatement[]{prepStmt});
        }

    }

    @Override
    public void doDeleteRole(String roleName) throws UserStoreException {
        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");
    }

    @Override
    public void doDeleteUser(String userName) throws UserStoreException {
        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");
    }

    @Override
    public boolean isBulkImportSupported() {
        return false;
    }

    @Override
    public void doUpdateRoleName(String roleName, String newRoleName) throws UserStoreException {
        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");
    }

   /* @Override
    public void doUpdateUserListOfRole(String roleName, String[] deletedUsers, String[] newUsers)
            throws UserStoreException {
        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");
    }*/

    @Override
    public void doUpdateRoleListOfUser(String userName, String[] deletedRoles, String[] newRoles)
            throws UserStoreException {
        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");
    }
    @Override
    public void doUpdateUserListOfRole(String roleName, String[] deletedUsers, String[] newUsers) throws UserStoreException {
        JDBCRoleContext ctx = (JDBCRoleContext)this.createRoleContext(roleName);
        roleName = ctx.getRoleName();
        int roleTenantId = ctx.getTenantId();
        boolean isShared = ctx.isShared();
        String sqlStmt1 = this.realmConfig.getUserStoreProperty(isShared?"RemoveUserFromSharedRoleSQL":"RemoveUserFromRoleSQL");
        if(sqlStmt1 == null) {
            throw new UserStoreException("The sql statement for remove user from role is null");
        } else {
            Connection dbConnection = null;

            try {
                dbConnection = this.getDBConnection();
                String e = DatabaseCreator.getDatabaseType(dbConnection);
                String sqlStmt2 = "INSERT INTO CUSTOMER_USER_ROLE (CUSTOMER_USER_ID, CUSTOMER_ROLE_ID) VALUES ((SELECT CUSTOMER_ID FROM CUSTOMER_DATA WHERE CUSTOMER_NAME=? ),(SELECT CUSTOMER_ROLE_ID FROM CUSTOMER_ROLE WHERE CUSTOMER_ROLE_NAME=? ))";
               // sqlStmt2 = this.realmConfig.getUserStoreProperty("AddUserToRoleSQL");



                if(newUsers != null) {

                  DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2, new Object[]{newUsers,roleName});

                }

                dbConnection.commit();
            } catch (SQLException var15) {
                throw new UserStoreException(var15.getMessage(), var15);
            } catch (Exception var16) {
                throw new UserStoreException(var16.getMessage(), var16);
            } finally {
                DatabaseUtil.closeAllConnections(dbConnection, new PreparedStatement[0]);
            }

        }
    }

    @Override
    public void doSetUserClaimValue(String userName, String claimURI, String claimValue,
                                    String profileName) throws UserStoreException {
        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");
    }

    @Override
    public void doSetUserClaimValues(String userName, Map<String, String> claims,
                                     String profileName) throws UserStoreException {
        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");
    }

    @Override
    public void doDeleteUserClaimValue(String userName, String claimURI, String profileName)
            throws UserStoreException {
        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");
    }

    @Override
    public void doDeleteUserClaimValues(String userName, String[] claims, String profileName)
            throws UserStoreException {
        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");
    }

    @Override
    public void doUpdateCredential(String userName, Object newCredential, Object oldCredential)
            throws UserStoreException {
        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");
    }

    @Override
    public void doUpdateCredentialByAdmin(String userName, Object newCredential) throws UserStoreException {
        if(!this.checkUserPasswordValid(newCredential)) {
            throw new UserStoreException("Credential not valid. Credential must be a non null string with following format, " + this.realmConfig.getUserStoreProperty("PasswordJavaRegEx"));
        } else {
            String sqlStmt = this.realmConfig.getUserStoreProperty("UpdateUserPasswordSQL");
            if(sqlStmt == null) {
                throw new UserStoreException("The sql statement for delete user claim value is null");
            } else {
                String saltValue = null;
                if("true".equalsIgnoreCase((String)this.realmConfig.getUserStoreProperties().get("StoreSaltedPassword"))) {
                    byte[] password = new byte[16];
                    this.random.nextBytes(password);
                    saltValue = Base64.encode(password);
                }
                String password1 = this.preparePassword((String)newCredential, saltValue);
                this.updateStringValuesToDatabase((Connection)null, sqlStmt, new Object[]{password1, Boolean.valueOf(false), new Date(), userName});
            }
        }
    }

    public String[] getExternalRoleListOfUser(String userName) throws UserStoreException {
        /*informix user store manager is supposed to be read only and users in the custom user store
          users in the custom user store are only assigned to internal roles. Therefore this method
          returns an empty string.
         */

        return new String[0];
    }


    @Override
    public boolean doCheckExistingRole(String roleName) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doCheckExistingUser(String userName) throws UserStoreException {

        return true;
    }
    @Override
    protected boolean checkUserPasswordValid(Object credential) throws UserStoreException {
        if(credential == null) {
            return false;
        } else if(!(credential instanceof String)) {
            throw new UserStoreException("Can handle only string type credentials");
        } else {
            String password = ((String)credential).trim();
            if(password.length() < 1) {
                return false;
            }
            return true;
        }
    }

    @Override
    public org.wso2.carbon.user.api.Properties getDefaultUserStoreProperties(){
        Properties properties = new Properties();
        properties.setMandatoryProperties(CustomUserStoreConstants.CUSTOM_UM_MANDATORY_PROPERTIES.toArray
                (new Property[CustomUserStoreConstants.CUSTOM_UM_MANDATORY_PROPERTIES.size()]));
        properties.setOptionalProperties(CustomUserStoreConstants.CUSTOM_UM_OPTIONAL_PROPERTIES.toArray
                (new Property[CustomUserStoreConstants.CUSTOM_UM_OPTIONAL_PROPERTIES.size()]));
        properties.setAdvancedProperties(CustomUserStoreConstants.CUSTOM_UM_ADVANCED_PROPERTIES.toArray
                (new Property[CustomUserStoreConstants.CUSTOM_UM_ADVANCED_PROPERTIES.size()]));
        return properties;
    }
}
