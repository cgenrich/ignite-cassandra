package com.example.ignite.cassandra;

import java.util.UUID;

public class CompositeKey {
	private UUID first;
	private UUID second;
	public CompositeKey(){}
	public CompositeKey(UUID first, UUID second) {
		super();
		this.first = first;
		this.second = second;
	}
	/**
	 * @return the first
	 */
	public UUID getFirst() {
		return first;
	}
	/**
	 * @param first the first to set
	 */
	public void setFirst(UUID first) {
		this.first = first;
	}
	/**
	 * @return the second
	 */
	public UUID getSecond() {
		return second;
	}
	/**
	 * @param second the second to set
	 */
	public void setSecond(UUID second) {
		this.second = second;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CompositeKey other = (CompositeKey) obj;
		if (first == null) {
			if (other.first != null)
				return false;
		} else if (!first.equals(other.first))
			return false;
		if (second == null) {
			if (other.second != null)
				return false;
		} else if (!second.equals(other.second))
			return false;
		return true;
	}
}
