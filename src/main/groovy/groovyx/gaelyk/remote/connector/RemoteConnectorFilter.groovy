package groovyx.gaelyk.remote.connector

import groovy.util.logging.Log
import groovyx.gaelyk.GaelykBindingEnhancer

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

import com.google.appengine.api.utils.SystemProperty
import com.google.appengine.tools.remoteapi.RemoteApiInstaller
import com.google.appengine.tools.remoteapi.RemoteApiOptions

@Log
class RemoteConnectorFilter implements Filter {

    static final String CONF_FILE_NAME = 'gaelyk-remote-connector.properties'
    
    private RemoteApiOptions options

    @Override public void destroy() {}
    
    @Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if(!request.__installer__){
            try {
                def remoteInstaller = new RemoteApiInstaller()
                remoteInstaller.install(options)
                request.__installer__ = remoteInstaller
            } catch(IllegalStateException e){
                if(e.message != 'remote API is already installed'){
                    throw e
                }
            }

                
                
        }
        
        

        chain.doFilter(request, response)
        
        if(request.__installer__){
            request.__installer__.uninstall()
            request.__installer__ = null
        }
    }
    @Override public void init(FilterConfig config) throws ServletException {
        if(!GaelykBindingEnhancer.localMode){
            return
        }
        
        InputStream confStream = getClass().getResourceAsStream("/$CONF_FILE_NAME")

        if(!confStream){
            log.info "Gaelky Remote Connector disabled, configuration file ${CONF_FILE_NAME} found."
            return
        }


        Properties props = new Properties()
        props.load(confStream)

        for(String required in [
            'appid',
            'username',
            'password'
        ]){
            if(props.contains(required)){
                log.warning "Connector disabled because '$required' is missing in configuration file $CONF_FILE_NAME"
                return
            }
        }

        SystemProperty.applicationId.set("s~$props.appid")
        
        options = new RemoteApiOptions()
                .server("${props.appid}.appspot.com", 443)
                .credentials(props.username, props.password)
        if(props.path){
            options.remoteApiPath(props.path)
        }

        RemoteApiInstaller installer = new RemoteApiInstaller()
        for (int i = 1; i <= 10; i++) {
            try {
                installer.install(options)
                log.info "Gaelyk Remote Connector running against https://${props.appid}.appspot.com${props.path ?: ''} using $props.username"
                break
            } catch(IllegalStateException e) {
                if(e.message == 'remote API is already installed'){
                    return
                }
                break
            } catch(e){
                log.info "Failed to install remote api - attempt $i"
                e.printStackTrace()
                if(i == 10){
                    return
                }
            }
        }
        try {
            // Update the options with reusable credentials so we can skip
            // authentication on subsequent calls.
            options.reuseCredentials(props.username, installer.serializeCredentials());
        } finally {
            installer.uninstall();
        }
    }
}
