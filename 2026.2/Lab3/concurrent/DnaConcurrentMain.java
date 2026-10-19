import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

public class DnaConcurrentMain {

    private static synchronized void adicionaTotal(long val) {
        total += val;
    }

    private static long total = 0;
    
    static class FileWorker implements Runnable {
        private final File file;
        private final String padrao;
        private IOException erro;

        FileWorker(File file, String padrao) {
            this.file = file;
            this.padrao = padrao;
        }

        @Override
        public void run() {
            try {
             adicionaTotal(countInFile(file, padrao));
            } catch (IOException e) {
                erro = e;
            }
        }

        IOException getError() {
            return erro;
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Uso: java DnaConcurrentMain DIRETORIO_ARQUIVOS PADRAO");
            System.err.println("Exemplo: java DnaConcurrentMain dna_inputs CGTAA");
            System.exit(1);
        }

        String dirName = args[0];
        String padrao = args[1];

        File dir = new File(dirName);
        if (!dir.isDirectory()) {
            System.err.println("Caminho não é dir: " + dirName);
            System.exit(2);
        }

        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));
        if (files == null || files.length == 0) {
            System.err.println("Nenhum arquivo .txt encontrado em: " + dirName);
            System.exit(3);
        }

        FileWorker[] workers = new FileWorker[files.length];
        Thread[] threads = new Thread[files.length];

        for (int i = 0; i < files.length; i++) {
            workers[i] = new FileWorker(files[i], padrao);
            threads[i] = new Thread(workers[i]);
            threads[i].start();
        }

        try {
            for (Thread t : threads) {
                t.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Execução interrompida");
            System.exit(5);
        }

        for (FileWorker w : workers) {
            if (w.getError() != null) {
                System.err.println("Erro ao ler:" + w.getError().getMessage());
                System.exit(4);
            }
        }

        System.out.println("Sequencia " + padrao + " foi encontrada " + total + " de vezes");
    }

    public static long countInFile(File file, String padrao) throws IOException {
        long total = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                if (!line.isEmpty()) {
                    total += countInSequence(line, padrao);
                }
            }
        }
        return total;
    }

    public static long countInSequence(String sequence, String padrao) {
        if (sequence == null || padrao == null) {
            return 0;
        }
        int n = sequence.length();
        int m = padrao.length();
        if (m == 0 || n < m) {
            return 0;
        }
        long count = 0;
        for (int i = 0; i <= n - m; i++) {
            if (sequence.regionMatches(false, i, padrao, 0, m)) {
                count++;
            }
        }
        return count;
    }
}