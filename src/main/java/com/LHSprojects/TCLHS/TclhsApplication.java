package com.LHSprojects.TCLHS;

import com.LHSprojects.TCLHS.Repository.Repository;

//import org.apache.catalina.startup.ClassLoaderFactory.Repository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.LHSprojects.TCLHS.Repository.TutorRepository;
@SpringBootApplication
public class TclhsApplication {

	public static void main(String[] args) {
		SpringApplication.run(TclhsApplication.class, args);
		System.out.println("Test");
		// Repository repo = new Repository();
		// TutorRepository tutorRepo = new TutorRepository();
		// repo.setAllTutors(tutorRepo.getAllTutors());

		// System.out.println(repo.getAllTutors().size());

}
}