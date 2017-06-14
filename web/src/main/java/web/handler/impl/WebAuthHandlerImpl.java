package web.handler.impl;

import anno.RequirePermissions;
import anno.RequireRoles;
import auth.CustomWebUser;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.AuthWebVerticle;
import web.handler.WebAuthHandler;
import web.util.PackageUtil;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by liulaoye on 17-6-12.
 * 自定义的权限检测模块
 */
public class WebAuthHandlerImpl implements WebAuthHandler{
    private static final Logger logger = LoggerFactory.getLogger( WebAuthHandlerImpl.class.getName() );
    private static final String PACKAGE_BASE = "web.handler.impl";

    private static class PermisstionAndRoleSet{
        private Set<String> permissions = new HashSet<>();
        private Set<String> roles = new HashSet<>();
        private Set<String> all = new HashSet<>();

//        public PermisstionAndRoleSet( Set<String> permissions, Set<String> roles ){
//            this.permissions = permissions;
//            this.roles = roles;
//            all.addAll( permissions );
//            all.addAll( roles );
//        }

        void addRoles( Set<String> rolesSet ){
            roles.addAll( rolesSet );
            all.addAll( roles );
        }

        void addPermissions( Set<String> permissionsSet ){
            permissions.addAll( permissionsSet );
            all.addAll( permissionsSet );
        }

        @Override
        public String toString(){
            return "PermisstionAndRoleSet{" +
                    "permissions=" + permissions +
                    ", roles=" + roles +
                    '}';
        }

        boolean isExist( Set<String> set ){
            for( String s : set ) {
                if( all.contains( s ) ) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 仅供内部使用，原则上初始化之后不允许修改，否则可能造成多线程竞争，如果需要修改，可考虑采用vertx.sharedData()
     */
    private static final Map<String, PermisstionAndRoleSet> authMap = new HashMap<>();

    static{
        List<Class<?>> list = PackageUtil.getClasses( PACKAGE_BASE );

        for( Class<?> clazz : list ) {
            parseClass( clazz );
        }
        logger.info( authMap.toString() );

    }

    @SuppressWarnings("unused")
    public WebAuthHandlerImpl( AuthProvider authProvider ){

    }

    private static void parseClass( Class<?> clazz ){
        final Method[] methods = clazz.getDeclaredMethods();

        for( Method method : methods ) {

            if( method.isAnnotationPresent( RequirePermissions.class ) || method.isAnnotationPresent( RequireRoles.class ) ) {
                final String clazzName = getClassName( clazz );
                PermisstionAndRoleSet roleAndPermisstionSet = new PermisstionAndRoleSet();

                if( method.isAnnotationPresent( RequirePermissions.class ) ) {
                    roleAndPermisstionSet.addPermissions( transSetFromStr( method.getDeclaredAnnotation( RequirePermissions.class ).value() ) );
                }
                if( method.isAnnotationPresent( RequireRoles.class ) ) {
                    roleAndPermisstionSet.addRoles( transSetFromStr( method.getDeclaredAnnotation( RequireRoles.class ).value() ) );

                }
                String key = clazzName + "/" + method.getName();
                authMap.put( key, roleAndPermisstionSet );
            }
        }

    }

    /**
     * 把逗号分割的字符串转成一个Set
     *
     * @param str 要分割的字符串
     * @return set
     */
    private static Set<String> transSetFromStr( String str ){
        return Arrays.stream( str.split( "," ) ).collect( Collectors.toSet() );
    }

    /**
     * 按照规则生成class的name
     * 去掉包前缀PACKAGE_BASE = "web.handler.impl"
     * 去掉类名中的Handler
     * 转换为小写
     *
     * @param clazz class
     * @return class name
     */
    private static String getClassName( Class<?> clazz ){
        String canonicalName = clazz.getCanonicalName();
        canonicalName = canonicalName.substring( PACKAGE_BASE.length() + 1 ).replace( "Handler", "" );
        return canonicalName.toLowerCase();
    }

    @Override
    public void handle( RoutingContext ctx ){
        final CustomWebUser user = (CustomWebUser) ctx.user();

        if( user == null ) {
            ctx.fail( 403 );

//           ctx.response().end("Not loggin");
        } else {
            String path = ctx.request().path().substring( AuthWebVerticle.API_PREFIX.length() );
            PermisstionAndRoleSet permissionOrRole = authMap.get( path );
            if( permissionOrRole == null ) {
                ctx.fail( 403 );
                return;
            }

            if( doIsPermitted( user, permissionOrRole ) )
                ctx.next();
            else
                ctx.fail( 403 );
        }
    }

    private boolean doIsPermitted( CustomWebUser user, PermisstionAndRoleSet permissionOrRole ){
        Set<String> roles = user.getRoles();
        Set<String> permissions = user.getPermissions();
        return roles.contains( "admin" ) || permissionOrRole.isExist( roles ) || permissionOrRole.isExist( permissions );

    }

}
