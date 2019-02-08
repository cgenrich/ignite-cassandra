package com.example.ignite.cassandra;

import com.google.protobuf.ByteString;

public class CompositeValue {
	private int third;
	private int fourth;
	private String fifth;
	private ByteString sixth;
	private ByteString seventh;
	private long eighth;
	public CompositeValue(){}
	public CompositeValue(int third, int fourth, String fifth, ByteString sixth, ByteString seventh, long eighth) {
		super();
		this.third = third;
		this.fourth = fourth;
		this.fifth = fifth;
		this.sixth = sixth;
		this.seventh = seventh;
		this.eighth = eighth;
	}
	/**
	 * @return the third
	 */
	public int getThird() {
		return third;
	}
	/**
	 * @param third the third to set
	 */
	public void setThird(int third) {
		this.third = third;
	}
	/**
	 * @return the fourth
	 */
	public int getFourth() {
		return fourth;
	}
	/**
	 * @param fourth the fourth to set
	 */
	public void setFourth(int fourth) {
		this.fourth = fourth;
	}
	/**
	 * @return the fifth
	 */
	public String getFifth() {
		return fifth;
	}
	/**
	 * @param fifth the fifth to set
	 */
	public void setFifth(String fifth) {
		this.fifth = fifth;
	}
	/**
	 * @return the sixth
	 */
	public ByteString getSixth() {
		return sixth;
	}
	/**
	 * @param sixth the sixth to set
	 */
	public void setSixth(ByteString sixth) {
		this.sixth = sixth;
	}
	/**
	 * @return the seventh
	 */
	public ByteString getSeventh() {
		return seventh;
	}
	/**
	 * @param seventh the seventh to set
	 */
	public void setSeventh(ByteString seventh) {
		this.seventh = seventh;
	}
	/**
	 * @return the eighth
	 */
	public long getEighth() {
		return eighth;
	}
	/**
	 * @param eighth the eighth to set
	 */
	public void setEighth(long eighth) {
		this.eighth = eighth;
	}
}
