package com.LHSprojects.TCLHS;

//import org.apache.catalina.startup.ClassLoaderFactory.Repository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TclhsApplication {

	public static void main(String[] args) {
		SpringApplication.run(TclhsApplication.class, args);

			//waiting for declarations to be fixed
			Repository repo = new Repository(); 
			System.out.println(repo.getAllTutors().size());
			
			for (int i = 0; i < repo.getAllTutors().size(); i++) {
				if (i = repo.getAllTutors().size() - 2) {
					System.out.print(repo.getAllTutors.get(i));
				} else {
					System.out.print(repo.getAllTutors.get(i) + ", ");
				}
			}

	}

}
