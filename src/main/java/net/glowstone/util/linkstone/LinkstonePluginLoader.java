package net.glowstone.util.linkstone;

import net.glowstone.linkstone.runtime.LinkstoneRuntimeData;
import net.glowstone.linkstone.runtime.boxing.BoxPatchVisitor;
import net.glowstone.linkstone.runtime.direct.DirectFieldAccessReplaceVisitor;
import net.glowstone.linkstone.runtime.inithook.ClassInitInvokeVisitor;
import net.glowstone.linkstone.runtime.reflectionredirect.field.FieldAccessorUtility;
import net.glowstone.linkstone.runtime.reflectionredirect.method.MethodAccessorUtility;
import net.glowstone.linkstone.runtime.reflectionreplace.ReflectionReplaceVisitor;
import org.apache.commons.lang.Validate;
import org.bukkit.Server;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.java.PluginClassLoader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class LinkstonePluginLoader extends JavaPluginLoader {
    /**
     * Bukkit will invoke this constructor via reflection.
     * Its signature should therefore not be changed!
     *
     * @param instance the server instance
     */
    public LinkstonePluginLoader(Server instance) {
        super(instance);
        LinkstoneRuntimeData.setPluginClassLoader(new ClassLoader() {
            @Override
            protected Class<?> findClass(String name) {
                return getClassByName(name);
            }
        });
    }

    @Override
    protected PluginClassLoader newPluginLoader(JavaPluginLoader loader, ClassLoader parent,
                                                PluginDescriptionFile description, File dataFolder, File file) throws Exception {
        return new PluginClassLoader(loader, parent, description, dataFolder, file) {
            @Override
            protected byte[] transformBytecode(byte[] bytecode) {
                if (LinkstoneRuntimeData.getFields().isEmpty()
                        && LinkstoneRuntimeData.getBoxes().isEmpty()) {
                    // There are no plugins installed that use a @LField or @LBox annotation
                    // so there's no need for runtime support
                    return bytecode;
                }
                ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

                ClassVisitor cv = cw;
                cv = new DirectFieldAccessReplaceVisitor(LinkstoneRuntimeData.getFields(), cv);
                if (!FieldAccessorUtility.isSupported() || !MethodAccessorUtility.isSupported()) {
                    cv = new ReflectionReplaceVisitor(cv);
                }
                cv = new ClassInitInvokeVisitor(cv);
                cv = new BoxPatchVisitor(LinkstoneRuntimeData.getBoxes(), cv);

                new ClassReader(bytecode).accept(cv, 0);
                return cw.toByteArray();
            }
        };
    }

    @Override
    public PluginDescriptionFile getPluginDescription(File file) throws InvalidDescriptionException {
        Validate.notNull(file, "File cannot be null");

        JarFile jar = null;
        InputStream stream = null;

        try {
            jar = new JarFile(file);
            JarEntry entry = jar.getJarEntry("glowstone.yml") != null ? jar.getJarEntry("glowstone.yml") : jar.getJarEntry("plugin.yml");

            if (entry == null) {
                throw new InvalidDescriptionException(new FileNotFoundException("Jar does not contain glowstone.yml or plugin.yml"));
            }

            stream = jar.getInputStream(entry);

            return new PluginDescriptionFile(stream);

        } catch (IOException | YAMLException ex) {
            throw new InvalidDescriptionException(ex);
        } finally {
            if (jar != null) {
                try {
                    jar.close();
                } catch (IOException ignored) {
                }
            }
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
