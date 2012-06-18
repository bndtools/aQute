package com.libsync.domain.repository;

import java.util.*;

public interface RepositoryManager {

	void createRepository(Repository repo);
	Collection<Repository> find();
}
