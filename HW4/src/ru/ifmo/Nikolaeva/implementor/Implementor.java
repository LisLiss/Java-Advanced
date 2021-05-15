package ru.ifmo.Nikolaeva.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static java.net.IDN.toUnicode;

public class Implementor implements Impler {
    private final static String TAB = "    ";
    private final static String SPACE = " ";
    private final static String COMMA = ",";
    private final static String ENDL = System.lineSeparator();
    private final static String SEMICOLON = ";";
    private final static String OPEN_BRACE = "{";
    private final static String CLOSE_BRACE = "}";
    private final static String IMPL = "Impl";

    private static String getPackage(Class<?> token) {
        StringBuilder ans = new StringBuilder();
        if (token.getPackage() != null) {
            ans.append("package").append(SPACE).append(token.getPackage()
                    .getName()).append(SEMICOLON).append(ENDL).append(ENDL);
        }
        return ans.toString();
    }

    private static String getClassName(Class<?> token) {
        return token.getSimpleName() + IMPL;
    }

    private static String getClassHeader(Class<?> token) {
        StringBuilder ans = new StringBuilder();
        ans.append("public class").append(SPACE).append(getClassName(token)).append(SPACE);
        if (token.isInterface()) {
            ans.append("implements");
        } else {
            ans.append("extends");
        }
        ans.append(SPACE).append(token.getSimpleName()).append(SPACE).append(OPEN_BRACE).append(ENDL);
        return ans.toString();
    }

    private static String getOverride(Executable executable) {
        StringBuilder ans = new StringBuilder();
        if (executable instanceof Method) {
            ans.append(TAB).append("@Override").append(ENDL);
        }
        return ans.toString();
    }

    private static String getDeprecated(Executable executable) {
        StringBuilder ans = new StringBuilder();
        if (executable instanceof Method) {
            ans.append(TAB).append("@Deprecated").append(ENDL);
        }
        return ans.toString();
    }

    private static String getNameExecutable(Executable executable) {
        StringBuilder ans = new StringBuilder();
        TypeVariable<?>[] paramTypes = executable.getTypeParameters();
        if (paramTypes.length > 0) {
            ans.append(Arrays.stream(paramTypes).map(TypeVariable::getTypeName)
                    .collect(Collectors.joining(COMMA, "<", ">")));
        }
        if (executable instanceof Method) {
            Method method = (Method) executable;
            ans.append(method.getGenericReturnType().getTypeName()).append(SPACE).append(method.getName());
        } else {
            Constructor constructor = (Constructor) executable;
            ans.append(getClassName(constructor.getDeclaringClass()));
        }
        return ans.toString();
    }

    private static String getParameter(Type type, Parameter parameter, boolean needed) {
        StringBuilder ans = new StringBuilder();
        if (needed) {
            ans.append(type.getTypeName().replaceAll("\\$", ".")).append(SPACE);
        }
        ans.append(parameter.getName());
        return ans.toString();
    }

    private static String getParameters(Executable executable, boolean needed) {
        Parameter[] parameters = executable.getParameters();
        Type[] types = executable.getGenericParameterTypes();
        StringJoiner ans = new StringJoiner(COMMA + SPACE, "(", ")");
        for (int i = 0; i < parameters.length; i++) {
            ans.add(getParameter(types[i], parameters[i], needed));
        }
        return ans.toString();
    }

    private static String getExceptions(Executable executable) {
        StringBuilder ans = new StringBuilder();
        Class<?>[] exceptions = executable.getExceptionTypes();
        if (exceptions.length > 0) {
            ans.append(SPACE).append("throws").append(SPACE)
                    .append(Arrays.stream(exceptions).map(Class::getCanonicalName)
                            .collect(Collectors.joining(COMMA + SPACE)));
        }
        return ans.toString();
    }

    private static String getDefaultReturnValue(Class<?> token) {
        StringBuilder ans = new StringBuilder(SPACE);
        if (token.equals(boolean.class)) {
            ans.append("false");
        } else if (token.equals(void.class)) {
            ans = new StringBuilder();
        } else if (token.isPrimitive()) {
            ans.append("0");
        } else {
            ans.append("null");
        }
        return ans.toString();
    }

    private static String getBody(Executable executable) {
        StringBuilder ans = new StringBuilder();
        if (executable instanceof Method) {
            ans.append("return").append(getDefaultReturnValue(((Method) executable).getReturnType()));
        } else {
            ans.append("super").append(getParameters(executable, false));
        }
        return ans.toString();
    }

    private static String getExecutable(Executable executable) {
        StringBuilder ans = new StringBuilder(TAB);
        final int modifiers = executable.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.NATIVE & ~Modifier.TRANSIENT;
        for (Annotation annotation : executable.getAnnotations()){
            if (annotation.toString().startsWith("@java.lang.Deprecated")){
                ans.append(getDeprecated(executable));
                break;
            }
        }
        ans.append(getOverride(executable)).append(Modifier.toString(modifiers)).append(modifiers > 0 ? SPACE : "")
                .append(getNameExecutable(executable)).append(getParameters(executable, true))
                .append(getExceptions(executable)).append(SPACE).append(OPEN_BRACE).append(ENDL).append(TAB + TAB)
                .append(getBody(executable)).append(SEMICOLON).append(ENDL).append(TAB).append(CLOSE_BRACE).append(ENDL);
        return ans.toString();
    }

    private static class MethodWrapper {
        private final static int BASE = 37;
        private final static int MOD = (int) 1e9 + 7;
        private Method method;

        MethodWrapper(Method method) {
            this.method = method;
        }

        Method getMethod() {
            return method;
        }

        @Override
        public boolean equals(Object object) {
            if (object == null) {
                return false;
            }
            if (object instanceof MethodWrapper) {
                MethodWrapper second = (MethodWrapper) object;
                return Arrays.equals(method.getParameterTypes(), second.method.getParameterTypes())
                        && method.getReturnType().equals(second.method.getReturnType())
                        && method.getName().equals(second.method.getName());
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return ((Arrays.hashCode(method.getParameterTypes())) % MOD + (BASE * method.getReturnType().hashCode()) % MOD +
                    (BASE * BASE * method.getName().hashCode()) % MOD);
        }
    }

    private static void getAbstractMethods(Method[] methods, Set<MethodWrapper> set) {
        Arrays.stream(methods).filter(method -> Modifier.isAbstract(method.getModifiers())).map(MethodWrapper::new)
                .collect(Collectors.toCollection(() -> set));
    }

    private static void implementAbstractMethods(Class<?> token, BufferedWriter writer) throws IOException {
        Set<MethodWrapper> methodWrappers = new HashSet<>();
        getAbstractMethods(token.getMethods(), methodWrappers);
        while (token != null) {
            getAbstractMethods(token.getDeclaredMethods(), methodWrappers);
            token = token.getSuperclass();
        }
        for (MethodWrapper methodWrapper : methodWrappers) {
            writer.write(toUnicode(getExecutable(methodWrapper.getMethod())));
        }
    }

    private static void implementConstructors(Class<?> token, BufferedWriter writer) throws IOException, ImplerException {
        Constructor<?>[] constructors;
        constructors = Arrays.stream(token.getDeclaredConstructors())
                .filter(constructor -> !(Modifier.isPrivate(constructor.getModifiers()))).toArray(Constructor[]::new);
        if (constructors.length == 0) {
            throw new ImplerException("Public constructors don't exist");
        }
        for (Constructor<?> constructor : constructors) {
            writer.write(toUnicode(getExecutable(constructor)));
        }
    }

    @Override
    public void implement(Class<?> token, Path path) throws ImplerException {
        if (token == null) {
            throw new ImplerException("Token is null");
        }
        if (path == null) {
            throw new ImplerException("Path is null");
        }
        if (token.isPrimitive() || token == Enum.class || token.isArray() || Modifier.isFinal(token.getModifiers())) {
            throw new ImplerException("Incorrect token");
        }
        path = path.resolve(token.getPackageName().replace('.', File.separatorChar))
                .resolve(token.getSimpleName() + IMPL + ".java");
        if (path.getParent() != null) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException e) {
                throw new ImplerException("Can't create directory");
            }
        }
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(getPackage(token));
            writer.write(toUnicode(getClassHeader(token)));
            if (!token.isInterface()) {
                implementConstructors(token, writer);
            }
            implementAbstractMethods(token, writer);
            writer.write(toUnicode(CLOSE_BRACE));
        } catch (IOException e) {
            throw new ImplerException("Can't write in file");
        }
    }

    public static void main(String[] args) {
        if (args == null) {
            System.out.println("Args is null");
            return;
        }
        if (args.length != 2) {
            System.out.println("Length of args is not two");
            return;
        }
        if (args[0] == null || args[1] == null) {
            System.out.println("Argument is null");
        }
        Implementor implementor = new Implementor();
        try {
            implementor.implement(Class.forName(args[0]), Paths.get(args[1]));
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found");
        } catch (ImplerException e) {
            System.out.println(e.getMessage());
        }
    }
}
