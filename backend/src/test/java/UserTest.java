// backend/src/test/java/com/project/thelittlethings/UserTest.java
import com.project.thelittlethings.TheLittleThingsApplication;
import com.project.thelittlethings.entities.User;
import com.project.thelittlethings.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.UUID;

@SpringBootTest(classes = TheLittleThingsApplication.class) // <— tie to your @SpringBootApplication
@ActiveProfiles("test")                                     // uses application-test.yml
@AutoConfigureTestDatabase(replace = Replace.NONE)          // don’t swap to H2
class UserTest {

    @Autowired private UserRepository userRepo;
    @Autowired private DataSource ds;

    @Test
    void testInsertUser_intoPostgres() throws Exception {
        // sanity: print the DB URL so you’re sure it’s Postgres
        try (var c = ds.getConnection()) {
            System.out.println("Connected to: " + c.getMetaData().getURL());
        }

        String suffix = UUID.randomUUID().toString().substring(0, 8);
        User u = new User();
        u.setUsername("testuser_" + suffix);
        u.setEmail("test_" + suffix + "@example.com");
        u.setPassword("secret123");
        u.setFirstName("Test");
        u.setLastName("User");
        u.setDob(LocalDate.of(2000, 1, 1));
        u.setAge(25);
        u.setGender("Male");
        u.setRegion("NSW");
        u.setStreaks(0);

        // force SQL to execute now
        var saved = userRepo.saveAndFlush(u);
        System.out.println("Inserted user id=" + saved.getUserId());
    }
}
