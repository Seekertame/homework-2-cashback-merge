package academy.cashbackmerge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;

/** Точка входа в Cashback Merge. Без команды печатает справку. */
@Command(
        name = "cashback-merge",
        mixinStandardHelpOptions = true,
        description = "Мини-игра Cashback Merge.",
        subcommands = InitCommand.class)
public final class Main implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private Main() {}

    public static void main(String[] args) {
        LOG.debug("Аргументы запуска: {}", String.join(" ", args));

        int exitCode = new CommandLine(new Main())
                .setExecutionExceptionHandler((error, commandLine, parseResult) -> {
                    // Ошибку во входных данных, например в конфиге, показываем текстом без стектрейса.
                    if (error instanceof IllegalArgumentException) {
                        commandLine.getErr().println(error.getMessage());
                        return 1;
                    }
                    throw error;
                })
                .execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }
}
