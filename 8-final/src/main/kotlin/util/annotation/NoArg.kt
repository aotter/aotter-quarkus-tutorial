package util.annotation

/**
 * Any class annotated with @NoArg will be process by kotlin no-arg plugin
 */
@Target(AnnotationTarget.CLASS)
annotation class NoArg
