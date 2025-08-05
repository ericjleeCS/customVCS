package gitlet;

import java.nio.file.Path;

public class Main {
  public static void main(String[] args) {
    try {
      if (args.length == 0) { printUsage(); return; }
      dispatch(args);
    } catch (Exception e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
  }

  static void dispatch(String[] args) throws Exception {
    String cmd = args[0];
    Path repoRoot = Path.of(System.getProperty("user.dir"));
    Repository repo = new Repository(repoRoot);

    switch (cmd) {
      case "init" -> repo.init();
      case "add" -> {
        if (args.length != 2) { printUsage(); return; }
        repo.add(args[1]);
      }
      case "rm" -> {
        if (args.length != 2) { printUsage(); return; }
        repo.remove(args[1]);
      }
      case "commit" -> {
        if (args.length < 2) { printUsage(); return; }
        String msg = args[1];
        repo.commit(msg);
      }
      case "status" -> repo.status();
      default -> printUsage();
    }
  }

  static void printUsage() {
    System.out.println(
        "usage: gitlet <command> [args]\n" +
        "  init\n" +
        "  add <path>\n" +
        "  rm <path>\n" +
        "  commit <message>\n" +
        "  status"
    );
  }
}
