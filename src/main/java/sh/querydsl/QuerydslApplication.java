package sh.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.persistence.EntityManager;

@SpringBootApplication
public class QuerydslApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuerydslApplication.class, args);
	}

//	//JPAQueryFactory bean으로 등록하여 사용하기
//	@Bean
//	JPAQueryFactory jpaQueryFactory(EntityManager em) {
//		return new JPAQueryFactory(em);
//	}
}
