package com.nosqlrevolution.annotation.schema;

import java.lang.annotation.*;

/**
 * The attachment type allows to index different “attachment” type field (encoded as base64), for example, microsoft office formats, open document formats, ePub, HTML, and so on.
 * 
 * The attachment type is provided as a plugin extension. The plugin is a simple zip file that can be downloaded and placed under $ES_HOME/plugins location. It will be automatically detected and the attachment type will be added.
 * 
 * Note, the attachment type is experimental.
 * 
 * @author cbrown
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
// TODO: Add more docs, posible object examples with all properties.
public @interface AttachementType {    
}
