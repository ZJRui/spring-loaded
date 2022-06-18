/*
 * Copyright 2010-2014 VMware and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springsource.loaded.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

/**
 * Basic agent implementation. This agent is declared in the META-INF/MANIFEST.MF file - that is how it is 'plugged in'
 * to the JVM when '-javaagent:springloaded.jar' is used.
 *
 *
 * idea中配置
 * -noverify -javaagent:/tmp/springloaded-1.3.0.RELEASE.jar -Dspringloaded="verbose=true;logging=true;watchJars=dependency.jar"
 *
 * 通过使用-javaagent参数，用户可以在执行main函数前执行指定javaagent包中指定的代码。javaagent代码和main方法在同一个JVM进程中运行，并被同一个sytemclassloader
 * 加载，被同一安全策略和上下文管理。
 *
 *
 * 关于-noverify
 *
 * 通过使用-vnoverify参数关闭java字节码的校验功能，当使用classloader加载字节码的时候，字节码首先接受校验器的校验。
 * JVM检查要加载的编译类的字节代码,以查看它是否表现良好.这是执行不受信任的代码的必要步骤.
 *
 * 不幸的是,这需要时间,对于像Eclipse这样的大型应用程序,这可能会增加启动时间. “-noverify”标志将其关闭.
 *听起来你需要在自己的字符串之后有一个空格,因此“-noverify”标志不会连接在一起.如果你不能这样做,那么就像“-Dignore”那样做一个变成-Dignore-noverify然后你的代码应该工作的解决方案.
 *
 * 启动时间，我会说。加载类时验证类是否正确需要一些时间。由于类可能以惰性方式加载（不是在应用程序启动时，而是在第一次使用时），这可能会导致意外和不希望的运行时延迟。
 * 实际上类一般不需要检查。编译器不会发出任何无效的字节码或类构造。进行验证的原因是该课程可能建立在一个系统上，在线托管并通过未受保护的互联网传输给您。
 * 在这条路径上，恶意攻击者可能会修改字节码并创建编译器可能永远不会创建的东西；可能会使 JVM 崩溃或可能绕过安全限制的东西。因此，在使用该类之前对其进行验证。如果这是本地应用程序，通常不需要再次检查字节码。
 *
 * 就是说你的字节码可能是从别处下载的，所以正常情况下需要校验。
 *
 *
 * 
 * @author Andy Clement
 * @since 0.5.0
 */
public class SpringLoadedAgent {

	private static ClassFileTransformer transformer = new ClassPreProcessorAgentAdapter();

	private static Instrumentation instrumentation;

	/**
	 * Spring boot 的实现 热加载的方式 ：spring loaded 和 devtools
	 * spring loader 是属于使用 Java agent 在应用运行前 指定  spring loader jar  的路径，然后 -java agent
	 * 或者使用maven 打包 ，然后使用maven 的命令行实现。
	 *
	 * 前提:自己在看如果实现热加载时，看到可以自定义的实现classloader 然后用一个线程去通过对比文件记录的LastModifedTime
	 * ，不断检查文件是否发生了改变，如果时间不对应，就要去利用自己的类加载器 加载一次改文件，实现了热加载。
	 * 从表面上来看没有什么问题，但实际你加载的对象和原来的对象是两个对象，spring loaded是如何将通过热加载的文件
	 * 重新指向之前的对象应该是一个要思考的问题。
	 * 
	 *
	 *
	 *
	 *
	 * @param options
	 * @param inst
	 */
	public static void premain(String options, Instrumentation inst) {
		// Handle duplicate agents
		if (instrumentation != null) {
			return;
		}
		instrumentation = inst;
		instrumentation.addTransformer(transformer);
	}

	public static void agentmain(String options, Instrumentation inst) {
		if (instrumentation != null) {
			return;
		}
		instrumentation = inst;
		instrumentation.addTransformer(transformer);
	}

	/**
	 * @return the Instrumentation instance
	 */
	public static Instrumentation getInstrumentation() {
		if (instrumentation == null) {
			throw new UnsupportedOperationException("Java 5 was not started with preMain -javaagent for SpringLoaded");
		}
		return instrumentation;
	}

}
