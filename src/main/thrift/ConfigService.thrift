namespace java net.thumbtack.configServer.thrift

exception DuplicateKeyException {
	1: string message
}

exception UnknownKeyException {
	1: string message
}

exception InvalidKeyException {
	1: string message
}

exception InvalidTimeoutException {
	1: string message
}

service ConfigService {

	/**
	 * Create a node with specific key
	 * @param key node key
	 * @throws DuplicateKeyException if node with specified key already exists
	 * @throws InvalidKeyException if specified key is empty or contains restricted chars
	 */
	void create(1: string key) throws (1: DuplicateKeyException ex1, 2: InvalidKeyException ex2)

	/**
	 * Create a node with specific key and value
	 * @param key node key
	 * @param value intial value to set
	 * @throws DuplicateKeyException if node with specified key already exists
	 * @throws InvalidKeyException if specified key is empty or contains restricted chars
	 */
	void createWithValue(1: string key, 2: string value) throws (1: DuplicateKeyException ex,
2: InvalidKeyException ex2)

	/**
	 * Create a temporary node with specific key and value.
	 * The node will disappear after the given timeout if the value will not be updated.
	 * If someone updates the value, the lifetime of the node will be prolonged by the initial timeout.
	 * @param key node key
	 * @param value intial value to set
	 * @param msTimeout timeout after which the node will be deleted if no updates occurred
	 * @throws DuplicateKeyException if node with specified key already exists
	 * @throws InvalidKeyException if specified key is empty or contains restricted chars
	 * @throws InvalidTimeoutException if the timeout is not positive
	 */
	void createTemporaryWithValue(1: string key, 2: string value, 3: i64 msTimeout) throws (1: DuplicateKeyException ex,
2: InvalidKeyException ex2, 3: InvalidTimeoutException ex3)

	/**
	 * Remove node by key
	 * @param key key of node to remove
	 * @throws UnknownKeyException if node with specified key doesn't exist
	 * @throws InvalidKeyException if specified key is empty or contains restricted chars
	 */
	void remove(1: string key) throws (1: UnknownKeyException ex1, 2: InvalidKeyException ex2)

	/**
	 * Check if node with specified key exists
	 * @param key key of node to check
	 * @return true if node exists, false otherwise
	 */
	bool exists(1: string key)

	/**
	 * Get value stored into node
	 * @param key key of node
	 * @return stored value or null if it wasn't set
	 * @throws UnknownKeyException if node with specified key doesn't exist
	 * @throws InvalidKeyException if specified key is empty or contains restricted chars
	 */
	string getValue(1: string key) throws (1: UnknownKeyException ex1, 2: InvalidKeyException ex2)

	/**
	 * Store value into specified node
	 * @param key key of node
	 * @param value value to store
	 * @throws UnknownKeyException if node with specified key doesn't exist
	 * @throws InvalidKeyException if specified key is empty or contains restricted chars
	 */
	void setValue(1: string key, 2: string value) throws (1: UnknownKeyException ex1,
2: InvalidKeyException ex2)

	/**
	 * Get list of children names for specific node
	 * @param key parent node key
	 * @throws UnknownKeyException if node with specified key doesn't exist
	 * @throws InvalidKeyException if specified key is empty or contains restricted chars
	 * @return sorted list of absolute keys for every node child
	 */
	list<string> getChildren(1: string key) throws (1: UnknownKeyException ex1,
2: InvalidKeyException ex2)
}