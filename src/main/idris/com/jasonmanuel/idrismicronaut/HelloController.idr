module com.jasonmanuel.HelloController

import IdrisJvm.IO
import IdrisJvm.System
import IdrisJvm.JvmImport
import Java.Lang
import Java.Util
import Java.Util.Function

exportController : FFI_Export FFI_JVM "HelloController" []
exportController =
    Fun classWith (Anns [ <@Controller> ["hello"] ]) $
    End
