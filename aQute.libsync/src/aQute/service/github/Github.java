package aQute.service.github;

public interface Github {
	public interface RepositoryBuilder {
		RepositoryBuilder owner(String owner);

		RepositoryBuilder authenticate(String user, String secret);

		Repository get();
	}

	RepositoryBuilder getRepository(String name);

}
