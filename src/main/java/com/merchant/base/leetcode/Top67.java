package com.merchant.base.leetcode;

/**
 * Description:
 * <p>
 *
 * @author lzm
 * @date 2026/1/15
 */
public class Top67 {

  public static void main(String[] args) {
    System.out.println(addBinary("11", "101"));
  }

  public static String addBinary(String a, String b) {

    int idxA = a.length() - 1;
    int idxB = b.length() - 1;
//    int right = Math.max(, );
    String result = "";
    int carry = 0;
    while (idxA >= -1 || idxB >= -1) {
      int intA = 0;
      if (idxA >= 0) {
        intA = a.charAt(idxA) - '0';
      }
      int intB = 0;
      if (idxB >= 0) {
        intB = b.charAt(idxB) - '0';
      }
      int sum = intA + intB;

      if (sum + carry == 1 || sum + carry == 0) {
        result = sum + carry + result;
        carry = 0;
      } else if (sum + carry == 2) {
        carry = 1;
        result = "0" + result;
      } else if (sum == 2) {
        carry = 1;
        result = "0" + result;
      }

      idxA--;
      idxB--;
    }

    return result;
  }

}
