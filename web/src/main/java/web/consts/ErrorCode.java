package web.consts;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liulaoye on 17-6-14.
 * ErrorCode
 */
@SuppressWarnings("unused")
public enum ErrorCode{

    SUCCESS( 0 ),

    ////////////////////////////////////////////////系统错误/////////////////////////////////////////

    SYS_UNKNOW_ERROR(1),

    /**
     * 无效的http请求
     */
    SYS_INVALID_REQUEST( 100 ),

    /**
     * 处理句柄不存在
     */
    SYS_HANDLER_NOT_FOUND( 101 ),

    /**
     * 客户端发送的签名字段验证错误
     */
    SYS_SIGNATURE_ERROR( 102 ),

    /**
     * 客户端发送请求参数错误
     */
    SYS_PARAMETER_ERROR( 103 ),

    /**
     * 方法未实现
     */
    SYS_NOT_IMPLENMENT( 104 ),

    ////////////////////////////////////////////////用户错误/////////////////////////////////////////
    /**
     * 尚未登录
     */
    USER_NOT_LOGIN( 1000 ),
    /**
     * 用户名或昵称重复
     */
    USER_DUPLICATE( 1001 ),
    /**
     * 已经登录
     */
    USER_HAS_LOGIN( 1002 ),
    /**
     * 用户不存在
     */
    USER_NOT_FOUND( 1003 ),
    /**
     * 用户名或密码错误
     */
    USER_UNAME_PASS_INVALID( 1004 ),
    USER_PERMISSION_DENY( 1005 );

    ////////////////////////////////////////////////枚举结束/////////////////////////////////////////


    private static final Map<Integer, ErrorCode> numToEnum = new HashMap<>();

    static{
        for( ErrorCode t : values() ) {

            ErrorCode s = numToEnum.put( t.number, t );
            if( s != null ) {
                throw new RuntimeException( t.number + "重复了" );
            }
        }
    }


    /**
     * 判断当前枚举代表的值是否成功
     *
     * @return true:成功 false:失败
     */
    public boolean isSuccess(){

        return number == 0;
    }

    private final int number;
    private final String msg;

    ErrorCode( int number ){
        this( number, null );

    }

    ErrorCode( int number, String msg ){
        this.number = number;
        this.msg = msg;
    }

    public static ErrorCode fromNum( int n ){
        return numToEnum.get( n );
    }

    public int toNum(){
        return number;
    }


    public static void main( String[] args ){
        System.out.println( ErrorCode.SUCCESS.toNum() );
    }

}

