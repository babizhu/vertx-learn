package web.handler.impl;

import anno.RequirePermissions;
import anno.RequireRoles;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    //    new HashMap<String,Set<String>>()
    Map<String, Set<String>> authMap = new HashMap<>();

    public WebAuthHandlerImpl( AuthProvider authProvider ){
        super();
        this.init();
    }

    private void init(){
        List<Class<?>> list = PackageUtil.getClasses( PACKAGE_BASE );

        for( Class<?> clazz : list ) {
            parseClass( clazz );
        }
        logger.info( authMap.toString() );

    }

    private void parseClass( Class<?> clazz ){
        final Method[] methods = clazz.getDeclaredMethods();
        final String clazzName = buildClassName( clazz );
        for( Method method : methods ) {
            Set<String> roleAndPermisstionSet = new HashSet<>();

//            System.out.println( method.getName() + (method.getDeclaredAnnotations()) );
            if( method.isAnnotationPresent( RequirePermissions.class ) || method.isAnnotationPresent( RequireRoles.class ) ) {
                if( method.isAnnotationPresent( RequirePermissions.class ) ) {
                    roleAndPermisstionSet.addAll(transSetFromStr( method.getDeclaredAnnotation( RequirePermissions.class ).value() ));
                }
                if( method.isAnnotationPresent( RequireRoles.class ) ) {
                    roleAndPermisstionSet.addAll(transSetFromStr( method.getDeclaredAnnotation( RequireRoles.class ).value() ));

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
    private Set<String> transSetFromStr( String str ){
        return Arrays.stream( str.split( "," ) ).collect( Collectors.toSet() );
//        final HashSet<String> set = new HashSet<>();
//        for( String s : str.split( "," ) ) {
//            set.add( s );
//        }
//        return set;
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
    private String buildClassName( Class<?> clazz ){
        String canonicalName = clazz.getCanonicalName();
        canonicalName = canonicalName.substring( PACKAGE_BASE.length() + 1 ).replace( "Handler", "" );
        return canonicalName.toLowerCase();
    }

    @Override
    public void handle( RoutingContext ctx ){

        ctx.next();
    }
}
