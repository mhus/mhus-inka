/**
 * Copyright 2018 Mike Hummel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.lib.core.util.lambda;

/**
 * Utility to help with lambda expressions. The main goal is to find the name of referenced method.
 * 
 * @author mikehummel
 *
 */
public class LambdaUtil {

//	public static boolean debugOut = false;
//	
//	public static String getDoubleUnaryOperatorName(DoubleUnaryOperator lambda) throws NotFoundException {
//		return getName(lambda);
//	}
//	
//	public static String getLongUnaryOperatorName(LongUnaryOperator lambda) throws NotFoundException {
//		return getName(lambda);
//	}
//	
//	public static String getIntUnaryOperatorName(IntUnaryOperator lambda) throws NotFoundException {
//		return getName(lambda);
//	}
//	
//	public static <T> String getToLongFunctionName(ToLongFunction<T> lambda) throws NotFoundException {
//		return getName(lambda);
//	}
//	
//	public static <T> String getToDoubleFunctionName(ToDoubleFunction<T> lambda) throws NotFoundException {
//		return getName(lambda);
//	}
//	
//	public static <T> String getToIntFunctionName(ToIntFunction<T> lambda) throws NotFoundException {
//		return getName(lambda);
//	}
//	
//	public static <T, U> String getBiPredicateName(BiPredicate<T, U> lambda) throws NotFoundException {
//		return getName(lambda);
//	}
//
//	public static String getLongPredicateName(LongPredicate lambda) throws NotFoundException {
//		return getName(lambda);
//	}
//	
//	public static String getDoublePredicateName(DoublePredicate lambda) throws NotFoundException {
//		return getName(lambda);
//	}
//	
//	public static String getIntPredicateName(IntPredicate lambda) throws NotFoundException {
//		return getName(lambda);
//	}
//	
//	public static <R> String getDoubleFunctionName(DoubleFunction<R> lambda) throws NotFoundException {
//		return getName(lambda);
//	}
//	
//	public static <R> String getLongFunctionName(LongFunction<R> lambda) throws NotFoundException {
//		return getName(lambda);
//	}
//	
//	public static <R> String getIntFunctionName(IntFunction<R> lambda) throws NotFoundException {
//		return getName(lambda);
//	}
//	
//	public static String getLongSupplierName(LongSupplier lambda) throws NotFoundException {
//		return getName(lambda);
//	}
//	
//	public static String getIntSupplierName(IntSupplier lambda) throws NotFoundException {
//		return getName(lambda);
//	}
//	
//	public static String getBooleanSupplierName(BooleanSupplier lambda) throws NotFoundException {
//		return getName(lambda);
//	}
//	
//	public static <T> String getPredicateName(Predicate<T> lambda) throws NotFoundException {
//		return getName(lambda);
//	}
//	
//	public static <T> String getBinaryOperatorName(BinaryOperator<T> lambda) throws NotFoundException {
//		return getName(lambda);
//	}
//	
//	public static <T,U,R> String getBiFunctionName(BiFunction<T,U,R> lambda) throws NotFoundException {
//		return getName(lambda);
//	}
//	
//	public static <T,R> String getBiConsumerName(BiConsumer<T,R> lambda) throws NotFoundException {
//		return getName(lambda);
//	}
//	
//	public static <T> String getConsumerName(Consumer<T> lambda) throws NotFoundException {
//		return getName(lambda);
//	}
//
//	public static String getSupplierName(Supplier<?> lambda) throws NotFoundException {
//		return getName(lambda);
//	}
//	
//	public static <T> String getFunctionName(Function<T,?> lambda) throws NotFoundException {
//		return getName(lambda);
//	}
//		
//	/*
//	 * Get and analyze the lambda byte code.
//	 * 
//
//---------------------
//inspect8_b132
//---------------------
//	 
//getClass:
//
//CAFEBABE 00000034 00140100 2564652F 6D687573  <00><00><00>4<00><14><01><00>%de/mhus
//2F6C6962 2F746573 742F4C61 6D626461 54657374  /lib/test/LambdaTest
//24244C61 6D626461 24310700 01010010 6A617661  $$Lambda$1<07><00><01><01><00><10>java
//2F6C616E 672F4F62 6A656374 07000301 001B6A61  /lang/Object<07><00><03><01><00><1B>ja
//76612F75 74696C2F 66756E63 74696F6E 2F46756E  va/util/function/Fun
//6374696F 6E070005 0100063C 696E6974 3E010003  ction<07><00><05><01><00><06><init><01><00><03>
//2829560C 00070008 0A000400 09010005 6170706C  ()V<0C><00><07><00><08><CR><00><04><00><TAB><01><00><05>appl
//79010026 284C6A61 76612F6C 616E672F 4F626A65  y<01><00>&(Ljava/lang/Obje
//63743B29 4C6A6176 612F6C61 6E672F4F 626A6563  ct;)Ljava/lang/Objec
//743B0100 244C6A61 76612F6C 616E672F 696E766F  t;<01><00>$Ljava/lang/invo
//6B652F4C 616D6264 61466F72 6D244869 6464656E  ke/LambdaForm$Hidden
//3B010008 67657443 6C617373 01001328 294C6A61  ;<01><00><08>getClass<01><00><13>()Lja
//76612F6C 616E672F 436C6173 733B0C00 0E000F0A  va/lang/Class;<0C><00><0E><00><0F><CR>
//00040010 01000443 6F646501 00195275 6E74696D  <00><04><00><10><01><00><04>Code<01><00><19>Runtim
//65566973 69626C65 416E6E6F 74617469 6F6E7310  eVisibleAnnotations<10>
//30000200 04000100 06000000 02000200 07000800  0<00><02><00><04><00><01><00><06><00><00><00><02><00><02><00><07><00><08><00>
//01001200 00001100 01000100 0000052A B7000AB1  <01><00><12><00><00><00><11><00><01><00><01><00><00><00><05>..<00><CR>.
//00000000 0001000B 000C0002 00120000 00110001  <00><00><00><00><00><01><00><0B><00><0C><00><02><00><12><00><00><00><11><00><01>
//00020000 00052BB6 0011B000 00000000 13000000  <00><02><00><00><00><05>+ﾶ<00><11>ﾰ<00><00><00><00><00><13><00><00><00>
//06000100 0D000000 00                          <06><00><01><00><BR><00><00><00><00>
//
//getMyDouble:
//
//CAFEBABE 00000034 001C0100 2564652F 6D687573  <00><00><00>4<00><1C><01><00>%de/mhus
//2F6C6962 2F746573 742F4C61 6D626461 54657374  /lib/test/LambdaTest
//24244C61 6D626461 24340700 01010010 6A617661  $$Lambda$4<07><00><01><01><00><10>java
//2F6C616E 672F4F62 6A656374 07000301 001B6A61  /lang/Object<07><00><03><01><00><1B>ja
//76612F75 74696C2F 66756E63 74696F6E 2F46756E  va/util/function/Fun
//6374696F 6E070005 0100063C 696E6974 3E010003  ction<07><00><05><01><00><06><init><01><00><03>
//2829560C 00070008 0A000400 09010005 6170706C  ()V<0C><00><07><00><08><CR><00><04><00><TAB><01><00><05>appl
//79010026 284C6A61 76612F6C 616E672F 4F626A65  y<01><00>&(Ljava/lang/Obje
//63743B29 4C6A6176 612F6C61 6E672F4F 626A6563  ct;)Ljava/lang/Objec
//743B0100 244C6A61 76612F6C 616E672F 696E766F  t;<01><00>$Ljava/lang/invo
//6B652F4C 616D6264 61466F72 6D244869 6464656E  ke/LambdaForm$Hidden
//3B01001C 64652F6D 6875732F 6C69622F 74657374  ;<01><00><1C>de/mhus/lib/test
//2F506F6A 6F457861 6D706C65 07000E01 000B6765  /PojoExample<07><00><0E><01><00><0B>ge
//744D7944 6F75626C 65010003 2829440C 00100011  tMyDouble<01><00><03>()D<0C><00><10><00><11>
//0A000F00 12010010 6A617661 2F6C616E 672F446F  <CR><00><0F><00><12><01><00><10>java/lang/Do
//75626C65 07001401 00077661 6C75654F 66010015  uble<07><00><14><01><00><07>valueOf<01><00><15>
//2844294C 6A617661 2F6C616E 672F446F 75626C65  (D)Ljava/lang/Double
//3B0C0016 00170A00 15001801 0004436F 64650100  ;<0C><00><16><00><17><CR><00><15><00><18><01><00><04>Code<01><00>
//1952756E 74696D65 56697369 626C6541 6E6E6F74  <19>RuntimeVisibleAnnot
//6174696F 6E731030 00020004 00010006 00000002  ations<10>0<00><02><00><04><00><01><00><06><00><00><00><02>
//00020007 00080001 001A0000 00110001 00010000  <00><02><00><07><00><08><00><01><00><1A><00><00><00><11><00><01><00><01><00><00>
//00052AB7 000AB100 00000000 01000B00 0C000200  <00><05>*ﾷ<00><CR>ﾱ<00><00><00><00><00><01><00><0B><00><0C><00><02><00>
//1A000000 17000200 02000000 0B2BC000 0FB60013  <1A><00><00><00><17><00><02><00><02><00><00><00><0B>..<00><0F>ﾶ<00><13>
//B80019B0 00000000 001B0000 00060001 000D0000  .<00><19>ﾰ<00><00><00><00><00><1B><00><00><00><06><00><01><00><BR><00><00>
//0000                                          <00><00>
//
//---------------------
//inspect8_121
//---------------------
//
//getText1:
//
//CAFEBABE 00000034 00140100 3E64652F 6D687573  <00><00><00>4<00><14><01><00>>de/mhus
//2F636865 7272792F 72656163 74697665 2F657861  /cherry/reactive/exa
//6D706C65 732F7369 6D706C65 312F5331 55736572  mples/simple1/S1User
//53746570 24244C61 6D626461 24313907 00010100  Step$$Lambda$19<07><00><01><01><00>
//106A6176 612F6C61 6E672F4F 626A6563 74070003  <10>java/lang/Object<07><00><03>
//01001B6A 6176612F 7574696C 2F66756E 6374696F  <01><00><1B>java/util/functio
//6E2F4675 6E637469 6F6E0700 05010006 3C696E69  n/Function<07><00><05><01><00><06><ini
//743E0100 03282956 0C000700 080A0004 00090100  t><01><00><03>()V<0C><00><07><00><08><CR><00><04><00><TAB><01><00>
//05617070 6C790100 26284C6A 6176612F 6C616E67  <05>apply<01><00>&(Ljava/lang
//2F4F626A 6563743B 294C6A61 76612F6C 616E672F  /Object;)Ljava/lang/
//4F626A65 63743B01 002F6465 2F6D6875 732F6368  Object;<01><00>/de/mhus/ch
//65727279 2F726561 63746976 652F6578 616D706C  erry/reactive/exampl
//65732F73 696D706C 65312F53 31506F6F 6C07000D  es/simple1/S1Pool<07><00><BR>
//01000867 65745465 78743101 00142829 4C6A6176  <01><00><08>getText1<01><00><14>()Ljav
//612F6C61 6E672F53 7472696E 673B0C00 0F00100A  a/lang/String;<0C><00><0F><00><10><CR>
//000E0011 01000443 6F646510 30000200 04000100  <00><0E><00><11><01><00><04>Code<10>0<00><02><00><04><00><01><00>
//06000000 02000200 07000800 01001300 00001100  <06><00><00><00><02><00><02><00><07><00><08><00><01><00><13><00><00><00><11><00>
//01000100 0000052A B7000AB1 00000000 0001000B  <01><00><01><00><00><00><05>....<00><CR>...<00><00><00><00><00><01><00><0B>
//000C0001 00130000 00140001 00020000 00082BC0  <00><0C><00><01><00><13><00><00><00><14><00><01><00><02><00><00><00><08>....
//000EB600 12B00000 00000000                    <00><0E>...<00><12>...<00><00><00><00><00><00>
//
//	 */
//	
////	static String getName(Object lambda) throws NotFoundException {
////		try {
////			Method m = lambda.getClass().getDeclaredMethod("writeReplace");
////			m.setAccessible(true);
////			SerializedLambda sl = (SerializedLambda) m.invoke(lambda);
////			return sl.getImplMethodName();
////		} catch (ReflectiveOperationException ex) {
////		}
////	}
//	
//	private static String getName(Object lambda) throws NotFoundException {
//		byte[] bc = null;
//		try {
//			bc = MSystem.getBytes(lambda.getClass());
//			if (debugOut)
//				System.out.println(MString.toHexDump(bc, 20));
//			// split the byte code
//			StringBuilder sb = new StringBuilder();
//			for (int i = 0; i < bc.length; i++) {
//				if (bc[i] < ' ' || bc[i] > 128)
//					sb.append('|');
//				else
//					sb.append((char)bc[i]);
//			}
//			
//			String code = sb.toString();
//			if (code.indexOf("java/lang/invoke/LambdaForm$Hidden;") > 0)
//				return inspect8_b132(lambda,bc,code);
//			
//			if (code.indexOf("&(Ljava/lang/Object;)Ljava/lang/Object;") > 0)
//				return inspect8_121(lambda,bc,code);
//
//		} catch (IOException e) {
//			throw new NotFoundException("method in lambda not found", MSystem.getJavaVersion(), lambda, MString.toHexDump(bc, 20), e);
//		}
//		throw new NotFoundException("method in lambda not found", MSystem.getJavaVersion(), MString.toHexDump(bc, 20), lambda);
//	}
//	
//	private static String inspect8_121(Object lambda, byte[] bc, String code) throws NotFoundException {
//		int pos = code.indexOf("&(Ljava/lang/Object;)Ljava/lang/Object;") + 40;
//		code = code.substring(pos);
//		String[] parts = code.split("\\|");
//		String last = null;
//		for (String p : parts) {
//			p = p.trim();
//			if (p.length() == 0) continue;
//			if (p.startsWith("()") && last != null) return last;
//			if (p.indexOf(';') == -1 && p.indexOf('/') == -1) last = p;
//		}
//		throw new NotFoundException("method in lambda not found", MSystem.getJavaVersion(), MString.toHexDump(bc, 20), lambda);
//	}
//	
//	private static String inspect8_b132(Object lambda, byte[] bc, String code) throws NotFoundException {
//		// http://grepcode.com/file/repository.grepcode.com/java/root/jdk/openjdk/8-b132/java/lang/invoke/LambdaForm.java
//		String[] parts = code.split("\\|");
//		// search for a valid candidate
//		boolean hiddenFound = false;
//		boolean ignoreFound = false;
//		for (String p : parts) {
//			p = p.trim();
//			if (p.indexOf("java/lang/invoke/LambdaForm$Hidden;") > 0) { // wait for the first 'Hidden'
//				hiddenFound = true;
//				continue;
//			}
//			if (hiddenFound) {
//				if (!ignoreFound && (
//					// ignore primitives for the first time
//					p.equals("intValue") || p.equals("longValue") || p.equals("doubleValue") ||
//					p.equals("booleanValue") || p.equals("floatValue") || p.equals("shortValue") ||
//					p.equals("byteValue") || p.equals("charValue")
//					)
//					) {
//					ignoreFound = true;
//					continue;
//				}
//				
//				// check for unwanted characters
//				if (p.length() > 1 && p.indexOf('/') < 0 && 
//						p.indexOf('$') < 0 && 
//						p.indexOf('<') < 0 && 
//						p.indexOf('(') < 0 &&
//						p.indexOf('[') < 0
//						)
//					return p; // not ... found it!
//			}
//		}
//		throw new NotFoundException("method in lambda not found", MSystem.getJavaVersion(), MString.toHexDump(bc, 20), lambda);
//	}
	
}
