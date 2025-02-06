#    Qverbow Util
#    Copyright (C) 2023 klxiaoniu
#
#    This program is free software: you can redistribute it and/or modify
#    it under the terms of the GNU Affero General Public License as
#    published by the Free Software Foundation, either version 3 of the
#    License, or (at your option) any later version.
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU Affero General Public License for more details.
#
#    You should have received a copy of the GNU Affero General Public License
#    along with this program.  If not, see <https://www.gnu.org/licenses/>.

-keep class com.xiaoniu.qqversionlist.data.** { *; }
-keep class com.tencent.kona.crypto.provider.* { *; }
-keep class com.fasterxml.jackson.core.type.TypeReference { *; }
-keep class com.zhipu.oapi.service.v4.api.* { *; }
-keep class com.zhipu.oapi.service.v4.model.* { *; }
-keep class io.reactivex.Single { *; }
-keep class org.kohsuke.github.* { *; }

-dontwarn com.sun.tools.javac.processing.JavacFiler
-dontwarn com.sun.tools.javac.processing.JavacProcessingEnvironment
-dontwarn com.sun.tools.javac.util.Context
-dontwarn com.sun.tools.javac.util.Options
-dontwarn java.beans.ConstructorProperties
-dontwarn java.beans.Transient
-dontwarn javax.lang.model.SourceVersion
-dontwarn javax.lang.model.element.AnnotationMirror
-dontwarn javax.lang.model.element.Element
-dontwarn javax.lang.model.element.ExecutableElement
-dontwarn javax.tools.Diagnostic$Kind
-dontwarn javax.tools.JavaFileManager$Location
-dontwarn javax.tools.JavaFileManager
-dontwarn javax.tools.StandardLocation
-dontwarn lombok.**
-dontwarn org.apache.tools.ant.BuildException
-dontwarn org.apache.tools.ant.Location
-dontwarn org.apache.tools.ant.Project
-dontwarn org.apache.tools.ant.Task
-dontwarn org.apache.tools.ant.types.FileSet
-dontwarn org.apache.tools.ant.types.Path
-dontwarn org.apache.tools.ant.types.Reference
-dontwarn org.apache.tools.ant.types.ResourceCollection
-dontwarn org.eclipse.**
-dontwarn org.osgi.framework.Bundle
-dontwarn org.osgi.framework.BundleContext
-dontwarn com.infradna.tool.bridge_method_injector.BridgeMethodsAdded
-dontwarn com.infradna.tool.bridge_method_injector.WithBridgeMethods
-dontwarn edu.umd.cs.findbugs.annotations.NonNull
-dontwarn edu.umd.cs.findbugs.annotations.SuppressFBWarnings
-dontwarn okhttp3.internal.annotations.EverythingIsNonNull
-dontwarn edu.umd.cs.findbugs.annotations.CheckForNull