package com.owlike.genson.ext;

import com.owlike.genson.Genson.Builder;
import com.owlike.genson.GensonBuilder;

/**
 * Bundles allow to package all kind of Genson customizations into a single module and register
 * them all together. Extensions are registered using Genson.Builder.
 * 
 * <pre>
 * Genson genson = new GensonBuilder().with(new SuperCoolExtension()).create();
 * </pre>
 * 
 * Extension configuration is mixed with user custom configuration (no way to distinguish them),
 * however user custom config. has preference over bundle configuration. This means that you can
 * override bundle configuration with custom one.
 * 
 * This part of the API is still in beta, it could change in the future in order to make it more
 * powerful.
 * 
 * @author eugen
 * 
 */
public abstract class GensonBundle {
	/**
	 * This method is called when all custom configuration has been registered. Use the builder to
	 * register your bundles.
	 */
	public abstract void configure(GensonBuilder builder);
}
