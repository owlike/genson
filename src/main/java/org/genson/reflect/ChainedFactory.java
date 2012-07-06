package org.genson.reflect;

import org.genson.Factory;
import org.genson.convert.Converter;

public abstract class ChainedFactory implements Factory<Converter<?>> {
	private Factory<? extends Converter<?>> next;
	
	protected ChainedFactory() {
	}
	
	protected ChainedFactory(Factory<Converter<?>> next) {
		this.next = next;
	}
	
	public <T extends Factory<? extends Converter<?>>> T withNext(T next) {
		this.next = next;
		return next;
	}
	
	public Factory<? extends Converter<?>> next() {
		return next;
	}
}
