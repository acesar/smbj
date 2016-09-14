/*
 * Copyright (C)2016 - SMBJ Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hierynomus.smbj.share

import spock.lang.Specification


public class RingBufferTest extends Specification {

  private RingBuffer cBuf;

  protected void setup() {
    cBuf = new RingBuffer(10);
  }

  def "should be able to append all bytes to buffer"() {
    given:
    def actual = [4, 5, 6, 7, 1] as byte[]
    def byteArray = [0, 0, 0, 0, 0] as byte[]


    when:
    cBuf.write(actual, 0, 5);

    then:
    cBuf.getUsedSize() == 5
    cBuf.read(byteArray)
    byteArray == actual
  }

  def "should be able to append only selected bytes to buffer"() {
    given:
    def actual = [4, 5, 6, 7, 1] as byte[];
    def byteArray = [0, 0, 0] as byte[]

    when:
    cBuf.write(actual, 2, 3);

    then:
    cBuf.getUsedSize() == 3
    cBuf.read(byteArray);
    byteArray == [6, 7, 1] as byte[]
  }

  def "should throw exception if bytes to write do not exist"() {
    given:
    def actual = [4, 5, 6, 7, 1] as byte[];

    when:
    cBuf.write(actual, 2, 4);

    then:
    thrown IllegalArgumentException
  }

  def "should be able to append single byte to buffer"() {
    given:
    def actual = [4, 5, 6, 7, 1] as byte[];
    def byteArray = [0, 0, 0, 0] as byte[]

    when:
    cBuf.write(actual, 2, 3);
    cBuf.write(2);

    then:
    cBuf.getUsedSize() == 4
    cBuf.read(byteArray);
    byteArray == [6, 7, 1, 2] as byte[]
  }

  def "should be able to append more bytes to existing buffer"() {
    given:
    def b1 = [4, 5, 6] as byte[]
    def b2 = [12, 13] as byte[]
    def byteArray = [0, 0, 0, 0, 0] as byte[]

    when:
    cBuf.write(b1, 0, 3);
    cBuf.write(b2, 0, 2);

    then:
    cBuf.getUsedSize() == 5
    cBuf.read(byteArray);
    byteArray == [4, 5, 6, 12, 13] as byte[]
  }

  def "should throw exception if buffer is full"() {
    given:
    def b = [4, 5, 6, 1, 2, 3, 4] as byte[];

    when:
    cBuf.write(b, 0, 6);
    cBuf.write(b, 0, 6);

    then:
    thrown IndexOutOfBoundsException
  }

  def "should read all available bytes from buffer"() {
    given:
    def b = [4, 5, 6, 7, 1] as byte[]
    def byteArray = [0, 0, 0, 0, 0] as byte[]

    when:
    cBuf.write(b, 0, 5);

    then:
    cBuf.getUsedSize() == 5
    cBuf.read(byteArray);
    byteArray == b
  }

  def "should read specified number of bytes from buffer"() {
    given:
    def b = [4, 5, 6, 7, 1, 20] as byte[]
    def b1 = [0, 0] as byte[]
    def b2 = [0, 0, 0] as byte[]
    def b3 = [0] as byte[]

    when:
    cBuf.write(b, 0, 6);

    then:
    cBuf.getUsedSize() == 6
    cBuf.read(b1);
    b1 == [4, 5] as byte[]
    cBuf.read(b2);
    b2 == [6, 7, 1] as byte[]
    cBuf.read(b3);
    b3 == [20] as byte[]
  }

  def "should throw exception if zero bytes are read"() {
    given:
    def b = [4, 5, 6] as byte[];

    when:
    cBuf.write(b, 0, 3);
    cBuf.read([] as byte[])

    then:
    thrown IllegalArgumentException
  }

  def "should indicate available bytes correctly"() {
    given:
    def b = [4, 5, 6, 10, 12] as byte[]

    when:
    cBuf.write(b, 0, 5);

    then:
    cBuf.getUsedSize() == 5
    cBuf.read([0] as byte[]);
    cBuf.getUsedSize() == 4
    cBuf.read([0, 0] as byte[]);
    cBuf.getUsedSize() == 2
    cBuf.read([0, 0] as byte[]);
    cBuf.getUsedSize() == 0
  }

  def "should be able to append more bytes if bytes are read"() {
    given:
    cBuf = new RingBuffer(3);
    def b1 = [4, 5, 6] as byte[]
    def byteArray = [0, 0, 0] as byte[]

    when:
    cBuf.write(b1, 0, 3);
    cBuf.read([0] as byte[]);
    cBuf.write([30] as byte[], 0, 1);


    then:
    cBuf.getUsedSize() == 3
    cBuf.read(byteArray);
    byteArray == [5, 6, 30] as byte[]
  }


  def "should be able to append more bytes if write position has wrapped around"() {
    given:
    cBuf = new RingBuffer(6);
    def b1 = [1, 2, 3, 4, 5, 6] as byte[]
    def byteArray = [0, 0, 0, 0, 0, 0] as byte[]

    when:
    cBuf.write(b1, 0, 6);
    cBuf.read([0, 0, 0, 0] as byte[]);
    cBuf.write([7, 8] as byte[], 0, 2);
    cBuf.write([9, 10] as byte[], 0, 2);


    then:
    cBuf.getUsedSize() == 6
    cBuf.read(byteArray);
    byteArray == [5, 6, 7, 8, 9, 10] as byte[]
  }

  def "should throw exception if full when write position has wrapped around"() {
    given:
    cBuf = new RingBuffer(6);
    def b = [1, 2, 3, 4, 5, 6] as byte[];

    when:
    cBuf.write(b, 0, 6);
    cBuf.read([0, 0, 0, 0] as byte[])
    cBuf.write([7, 8] as byte[], 0, 2);
    cBuf.write([9, 10, 11] as byte[], 0, 3);

    then:
    thrown IndexOutOfBoundsException
  }

}
