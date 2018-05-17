package fr.inria.spirals.repairnator.ghfilter;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ForkFilter {

    // this script intends to filter from a list the forks if the original repo is already on the list
    // or if another fork is already on the list
    public static void main(String[] args) throws IOException {
        String ghToken = args[0];
        String inputFilename = args[1];
        String outputFilename = args[2];

        GitHub gitHub = new GitHubBuilder().withOAuthToken(ghToken).build();
        int rateLimit = gitHub.getRateLimit().limit;

        if (rateLimit != 5000) {
            System.err.println("Please check your github token.");
            System.exit(1);
        }

        File inputFile = new File(inputFilename);
        if (!inputFile.exists()) {
            System.err.println("Please specify an existing input file");
            System.exit(1);
        }

        File outputFile = new File(outputFilename);
        if (outputFile.exists()) {
            System.err.println("Please specify an output file that does not exist");
            System.exit(1);
        }


        List<String> repoSlugs = Files.readAllLines(inputFile.toPath());

        Set<GHRepository> parentRepositories = new HashSet<>();
        List<String> result = new ArrayList<>();


        for (String repoSlug : repoSlugs) {
            GHRepository repository = gitHub.getRepository(repoSlug);

            if (repository.isFork()) {
                GHRepository parentRepo = repository.getParent();
                if (!parentRepositories.contains(parentRepo)) {
                    parentRepositories.add(parentRepo);
                    result.add(repository.getFullName());
                }
            } else {
                parentRepositories.add(repository);
                result.add(repository.getFullName());
            }
        }

        System.out.println("Obtained results: " + result.size() + " repositories on " + repoSlugs.size() + " originally.");

        try {
            Files.write(outputFile.toPath(), result);
        } catch (IOException e) {
            System.err.println("Error while writing file. Results will be displayed in standard output");
            for (String s : result) {
                System.out.println(s);
            }
        }
    }
}
