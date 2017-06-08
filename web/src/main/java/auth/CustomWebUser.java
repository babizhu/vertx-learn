package auth;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.impl.ClusterSerializable;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Created by liulaoye on 17-6-7.
 * 自定义User
 */
public class CustomWebUser implements User, ClusterSerializable{

    private final Set<String> roles;
    private final Set<String> permissions;
    private final JsonObject principal;

    private String rolePrefix;
    private CustomJdbcAuth authProvider;
    private String userName;

    CustomWebUser( String userName, Set<String> roles, Set<String> permissions, CustomJdbcAuth customJdbcAuth ){
        this.roles = roles;
        this.permissions = permissions;
        principal = new JsonObject().put( "username", userName );
        this.userName = userName;
        rolePrefix = CustomJdbcAuth.DEFAULT_ROLE_PREFIX;
    }


    @Override
    public User isAuthorised( String authority, Handler<AsyncResult<Boolean>> resultHandler ){
        if( authority.startsWith( rolePrefix ) ) {
            resultHandler.handle( Future.succeededFuture( roles.contains( authority.substring( rolePrefix.length() ) ) ) );
        } else {
            resultHandler.handle( Future.succeededFuture( permissions.contains( authority ) ) );
        }
        return this;
    }

    @Override
    public User clearCache(){
        roles.clear();
        permissions.clear();
        return this;
    }

    @Override
    public JsonObject principal(){

        return principal;
    }

    @Override
    public void setAuthProvider( AuthProvider authProvider ){
        if( authProvider instanceof CustomJdbcAuth ) {
            this.authProvider = (CustomJdbcAuth) authProvider;
        } else {
            throw new IllegalArgumentException( "Not a CustomJdbcAuth" );
        }
    }

    @Override
    public void writeToBuffer( Buffer buff ){

        writeStringSet( buff, roles );
        writeStringSet( buff, permissions );
        byte[] bytes = userName.getBytes( StandardCharsets.UTF_8 );
        buff.appendInt( bytes.length );
        buff.appendBytes( bytes );

        bytes = rolePrefix.getBytes( StandardCharsets.UTF_8 );
        buff.appendInt( bytes.length ).appendBytes( bytes );
    }

    @Override
    public int readFromBuffer( int pos, Buffer buffer ){
        pos = readStringSet( buffer, roles, pos );
        pos = readStringSet( buffer, permissions, pos );
//        pos = super.readFromBuffer(pos, buffer);

        int len = buffer.getInt( pos );
        pos += 4;
        byte[] bytes = buffer.getBytes( pos, pos + len );
        userName = new String( bytes, StandardCharsets.UTF_8 );
        pos += len;

        len = buffer.getInt( pos );
        pos += 4;
        bytes = buffer.getBytes( pos, pos + len );
        rolePrefix = new String( bytes, StandardCharsets.UTF_8 );
        pos += len;

        return pos;
    }


    private void writeStringSet( Buffer buff, Set<String> set ){
        buff.appendInt( set == null ? 0 : set.size() );
        if( set != null ) {
            for( String entry : set ) {
                byte[] bytes = entry.getBytes( StandardCharsets.UTF_8 );
                buff.appendInt( bytes.length ).appendBytes( bytes );
            }
        }
    }

    private int readStringSet( Buffer buffer, Set<String> set, int pos ){
        int num = buffer.getInt( pos );
        pos += 4;
        for( int i = 0; i < num; i++ ) {
            int len = buffer.getInt( pos );
            pos += 4;
            byte[] bytes = buffer.getBytes( pos, pos + len );
            pos += len;
            set.add( new String( bytes, StandardCharsets.UTF_8 ) );
        }
        return pos;
    }

    public Set<String> getRoles(){
        return roles;
    }
}
