/*
package dev.ltocca.loanranger;

import dev.ltocca.loanranger.presentationLayer.MainCLI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LoanRangerApplication {

*/
/*
    public static void main(String[] args) {
        SpringApplication.run(LoanRangerApplication.class, args);
    }
*//*


    public static void main(String[] args) {
        // Optionally run SpringApplication if needed for other features
        // SpringApplication.run(LoanRangerApplication.class, args);

        // Run the CLI
        try {
            MainCLI.main(args);
        } catch (Exception e) {
            System.err.println("Error running CLI: " + e.getMessage());
        }
    }
}
*/

package dev.ltocca.loanranger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LoanRangerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoanRangerApplication.class, args);
    }

}
