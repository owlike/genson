package org.genson.reflect;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.genson.TransformationRuntimeException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;

public final class ASMCreatorParameterNameResolver implements PropertyNameResolver {
	/**
	 * Wether we must throw an exception when we encounter a class compiled with no debug information.
	 */
	private final boolean doThrowException;
	private final Map<Constructor<?>, String[]> constructorParameterNames = new ConcurrentHashMap<Constructor<?>, String[]>();
	private final Map<Method, String[]> methodParameterNames = new ConcurrentHashMap<Method, String[]>();

	public ASMCreatorParameterNameResolver(boolean doThrowException) {
		this.doThrowException = doThrowException;
	}
	
	protected void read(Class<?> ofClass) {
		String ofClassName = ofClass.getName();
		ofClassName = ofClassName.replace('.', '/') + ".class";
		InputStream is = ASMCreatorParameterNameResolver.class.getClassLoader()
				.getResourceAsStream(ofClassName);

		ClassReader cr;
		ClassConstructorsVisitor visitor = new ClassConstructorsVisitor(ofClass,
				constructorParameterNames, methodParameterNames);
		try {
			cr = new ClassReader(is);
			cr.accept(visitor, 0);
		} catch (IOException e) {
			// C'est ok pas grave, cette technique n'est pas cense marcher dans tous les cas
		} finally {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
	}

	@Override
	public String resolve(int parameterIdx, Constructor<?> fromConstructor) {
		String[] names = constructorParameterNames.get(fromConstructor);
		if (names == null) {
			read(fromConstructor.getDeclaringClass());
			names = constructorParameterNames.get(fromConstructor);
		}

		if (names == null || names.length <= parameterIdx) {
			if (doThrowException) _throwNoDebugInfo(fromConstructor.getDeclaringClass().getName());
			return null;
		}
		
		return names[parameterIdx];
	}

	@Override
	public String resolve(Field fromField) {
		return null;
	}

	@Override
	public String resolve(Method fromMethod) {
		return null;
	}

	@Override
	public String resolve(int parameterIdx, Method fromMethod) {
		String[] names = methodParameterNames.get(fromMethod);
		if (names == null) {
			read(fromMethod.getDeclaringClass());
			names = methodParameterNames.get(fromMethod);
		}

		if (names == null || names.length <= parameterIdx) {
			if (doThrowException) _throwNoDebugInfo(fromMethod.getDeclaringClass().getName());
			return null;
		}
		
		return names[parameterIdx];
	}
	
	private void _throwNoDebugInfo(String className) {
		throw new TransformationRuntimeException("Class " + className + " has been compiled with no debug information, so we can not deduce constructor/method parameter names.");
	}

	private class ClassConstructorsVisitor extends EmptyVisitor {
		private final static String CONSTRUCTOR_METHOD_NAME = "<init>";

		private final Class<?> forClass;
		final Map<Constructor<?>, String[]> ctrParameterNames;
		final Map<Method, String[]> methodParameterNames;

		public ClassConstructorsVisitor(Class<?> forClass,
				Map<Constructor<?>, String[]> ctrParameterNames,
				Map<Method, String[]> methodParameterNames) {
			this.forClass = forClass;
			this.ctrParameterNames = ctrParameterNames;
			this.methodParameterNames = methodParameterNames;
		}

		public MethodVisitor visitMethod(int access, String name, String desc, String signature,
				String[] exceptions) {
			boolean ztatic = (access & Opcodes.ACC_STATIC) > 0;
			if (Opcodes.ACC_PRIVATE != access && Opcodes.ACC_PROTECTED != access
					&& Opcodes.ACC_ABSTRACT != access) {
				if (CONSTRUCTOR_METHOD_NAME.equals(name))
					return new ConstructorVisitor(forClass, ztatic, desc, ctrParameterNames);
				
				if ( !"<clinit>".equals(name) )
					return new NameMethodVisitor(name, forClass, ztatic, desc, methodParameterNames);
			} 
			return null;
		}

	}

	private abstract class BaseMethodVisitor extends EmptyVisitor {
		protected Type[] paramTypes;
		protected ArrayList<String> paramNames;
		protected final Class<?> forClass;
		protected boolean ztatic;
		
		public BaseMethodVisitor(Class<?> forClass, boolean ztatic, String desc,
				Map<Method, String[]> parameterNamesMap) {
			this.forClass = forClass;
			this.ztatic = ztatic;
			paramTypes = Type.getArgumentTypes(desc);
			paramNames = new ArrayList<String>(paramTypes.length);
		}

		public void visitLocalVariable(String variableName, String desc, String sig, Label start,
				Label end, int index) {

			if (!ztatic) {
				index--;
			}
			if (index >= 0 && paramNames.size() < paramTypes.length) {
				paramNames.add(variableName);
			}
		}

		protected Class<?> resolveClass(Type type) {
			switch (type.getSort()) {
			case Type.ARRAY:
				Class<?> componentClass = resolveClass(type.getElementType());
				return Array.newInstance(componentClass, 0).getClass();
			case Type.BOOLEAN:
				return boolean.class;
			case Type.BYTE:
				return byte.class;
			case Type.CHAR:
				return char.class;
			case Type.DOUBLE:
				return double.class;
			case Type.FLOAT:
				return float.class;
			case Type.INT:
				return int.class;
			case Type.LONG:
				return long.class;
			case Type.OBJECT: {
				try {
					return Class.forName(type.getClassName());
				} catch (ClassNotFoundException e) {
					throw new TransformationRuntimeException("Could not found class "
							+ type.getClassName() + " while searching for constructor "
							+ signature() + " parameter names.", e);
				}
			}
			case Type.SHORT:
				return short.class;
			case Type.VOID:
				return void.class;

			default:
				throw new TransformationRuntimeException(
						"Could not find corresponding java type to asm type " + type);
			}

		}

		public abstract String signature();
	}

	private class NameMethodVisitor extends BaseMethodVisitor {
		private final Map<Method, String[]> parameterNamesMap;
		private String name;
		
		public NameMethodVisitor(String name, Class<?> forClass, boolean ztatic, String desc,
				Map<Method, String[]> parameterNamesMap) {
			super(forClass, ztatic, desc, parameterNamesMap);
			this.parameterNamesMap = parameterNamesMap;
			this.name = name;
		}

		public void visitEnd() {
			if (paramNames.size() == paramTypes.length) {
				Method method = null;
				Class<?>[] javaTypes = new Class<?>[paramTypes.length];

				for (int i = 0; i < paramTypes.length; i++)
					javaTypes[i] = resolveClass(paramTypes[i]);

				try {
					method = forClass.getMethod(name, javaTypes);
					parameterNamesMap
							.put(method, paramNames
									.toArray(new String[paramNames.size()]));
				} catch (SecurityException e) {
					throw new TransformationRuntimeException(
							"Unable to locate method with signature " + signature(), e);
				} catch (NoSuchMethodException e) {
					// hum don't do anything... as we accept that it may fail...
				}
			}
		}
		
		@Override
		public String signature() {
			StringBuilder sb = new StringBuilder(name).append("(");
			for (int i = 0; i < paramTypes.length; i++) {
				String paramName = paramNames.isEmpty() ? "?"
						: (String) paramNames.get(i);
				sb.append(paramTypes[i].getClassName()).append(" " + paramName);
				if (i < paramTypes.length - 1) {
					sb.append(", ");
				}
			}
			sb.append(")");

			return sb.toString();
		}
	}

	private class ConstructorVisitor extends BaseMethodVisitor {
		private final Map<Constructor<?>, String[]> parameterNamesMap;

		public ConstructorVisitor(Class<?> forClass, boolean ztatic, String desc,
				Map<Constructor<?>, String[]> parameterNamesMap) {
			super(forClass, ztatic, desc, methodParameterNames);
			this.parameterNamesMap = parameterNamesMap;
		}

		public void visitEnd() {
			if (paramNames.size() == paramTypes.length) {
				Constructor<?> constructor = null;
				Class<?>[] javaTypes = new Class<?>[paramTypes.length];

				for (int i = 0; i < paramTypes.length; i++)
					javaTypes[i] = resolveClass(paramTypes[i]);

				try {
					constructor = forClass.getDeclaredConstructor(javaTypes);
					parameterNamesMap
							.put(constructor, paramNames
									.toArray(new String[paramNames.size()]));
				} catch (SecurityException e) {
					throw new TransformationRuntimeException(
							"Unable to locate constructor with signature " + signature(), e);
				} catch (NoSuchMethodException e) {
					// hum don't do anything... as we accept that it may fail...
				}
			}
		}

		public String signature() {
			StringBuilder sb = new StringBuilder(forClass.getSimpleName()).append("(");
			for (int i = 0; i < paramTypes.length; i++) {
				String paramName = paramNames.isEmpty() ? "?"
						: (String) paramNames.get(i);
				sb.append(paramTypes[i].getClassName()).append(" " + paramName);
				if (i < paramTypes.length - 1) {
					sb.append(", ");
				}
			}
			sb.append(")");

			return sb.toString();
		}
	}
}
