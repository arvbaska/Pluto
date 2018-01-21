/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.sql.DataSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;
import static javax.measure.unit.SI.KILOGRAM;
import javax.measure.quantity.Mass;
import org.jscience.physics.model.RelativisticModel;
import org.jscience.physics.amount.Amount;

@Controller
@SpringBootApplication
public class Main {

	
	boolean init = false;
	@Value("${spring.datasource.url}")
	private String dbUrl;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private DataSource dataSource;

	public static void main(String[] args) throws Exception {
		SpringApplication.run(Main.class, args);
	}

	@RequestMapping("/hello")
	String hello(Map<String, Object> model) {
		RelativisticModel.select();
		Amount<Mass> m = Amount.valueOf("12 GeV").to(KILOGRAM);
		model.put("science", "E=mc^2: 12 GeV = " + m.toString());
		return "hello";
	}

	@RequestMapping("/")
	String index() {
		return "index";
	}

	@RequestMapping("/ledOn")
	String ledOn(Map<String, Object> model) {

		try {
			final Resource fileResource = resourceLoader.getResource("classpath:json/jarvis.json");
			InputStream config = fileResource.getInputStream();

			FirebaseOptions options;
			options = new FirebaseOptions.Builder().setCredentials(GoogleCredentials.fromStream(config))
					.setDatabaseUrl("https://jarvis-b9456.firebaseio.com/").build();
			if(init==false) {
				FirebaseApp.initializeApp(options);
				init = true;
			}

			DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/LEDStatus");
			ref.setValueAsync(1);
			model.put("science",ref);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "hello";
	}

	@RequestMapping("/ledOff")
	String ledOff(Map<String, Object> model) {
		
		try {
			final Resource fileResource = resourceLoader.getResource("classpath:json/jarvis.json");
			InputStream config = fileResource.getInputStream();

			FirebaseOptions options;
			options = new FirebaseOptions.Builder().setCredentials(GoogleCredentials.fromStream(config))
					.setDatabaseUrl("https://jarvis-b9456.firebaseio.com/").build();
			if(init==false) {
				FirebaseApp.initializeApp(options);
				init = true;
			}

			DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/LEDStatus");
			ref.setValueAsync(0);
			model.put("science",ref);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "hello";
	}

	@RequestMapping("/db")
	String db(Map<String, Object> model) {
		try (Connection connection = dataSource.getConnection()) {
			Statement stmt = connection.createStatement();
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
			stmt.executeUpdate("INSERT INTO ticks VALUES (now())");
			ResultSet rs = stmt.executeQuery("SELECT tick FROM ticks");

			ArrayList<String> output = new ArrayList<String>();
			while (rs.next()) {
				output.add("Read from DB: " + rs.getTimestamp("tick"));
			}

			model.put("records", output);
			return "db";
		} catch (Exception e) {
			model.put("message", e.getMessage());
			return "error";
		}
	}

	@Bean
	public DataSource dataSource() throws SQLException {
		if (dbUrl == null || dbUrl.isEmpty()) {
			return new HikariDataSource();
		} else {
			HikariConfig config = new HikariConfig();
			config.setJdbcUrl(dbUrl);
			return new HikariDataSource(config);
		}
	}

	private static String getStringFromInputStream(InputStream is) {

		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {

			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return sb.toString();

	}

}
