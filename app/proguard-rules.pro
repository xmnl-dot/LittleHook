-keep class io.github.xtrlumen.littlehook.Entry
-assumenosideeffects class io.github.libxposed.api.XposedModule {
    public void log(...);
}

-repackageclasses
-overloadaggressively
-allowaccessmodification