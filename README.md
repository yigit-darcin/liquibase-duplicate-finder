# liquibase-duplicate-finder
Finding duplicate ids in liquibase files.

You can just install it to your local repo as mvn clean install

You can use it as :

                    <plugin>
                        <groupId>com.yigitdarcin</groupId>
                        <artifactId>liquibase-duplicate-finder</artifactId>
                        <version>0.1</version>
                        <configuration>
                            <location>your path to liquibase files</location>
                        </configuration>
                        <executions>
                            <execution>
                                <phase>test</phase>
                                <goals>
                                    <goal>find-duplicate-ids</goal>
                                </goals>
                            </execution>
                        </executions>
                    </plugin>

