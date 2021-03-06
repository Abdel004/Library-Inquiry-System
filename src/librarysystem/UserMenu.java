package librarysystem;

import java.sql.*;
import java.util.Scanner;
import java.util.ArrayList;

public class UserMenu {

	int option;
	Connection conn;

	public UserMenu(Connection conn) {
		this.option = 0;
		this.conn = conn;
	}

	// helper function to search for records by using callNum
	public void Search(String callNum) {
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			ArrayList<String> authors = new ArrayList<String>();
			pstm = conn.prepareStatement("SELECT title, rating, bcid from book WHERE callnum = ?");
			pstm.setString(1, callNum);
			rs = pstm.executeQuery();
			if (rs.next()) {
				String title = rs.getString("title");
				float rating = rs.getFloat("rating");
				if (rs.wasNull())
					rating = -1;
				String bcid = rs.getString("bcid");

				pstm = conn.prepareStatement("SELECT aname from authorship WHERE callnum = ?");
				pstm.setString(1, callNum);
				// creating a result set to handle multiple authors
				ResultSet multipleAuthors = pstm.executeQuery();
				while (multipleAuthors.next()) {
					// if multiple authors exist for a book, all of them are added to the ArrayList
					// 'authors'
					authors.add(multipleAuthors.getString(1));
				}

				pstm = conn.prepareStatement("SELECT bcname from book_category WHERE bcid = ?");
				pstm.setString(1, bcid);
				rs = pstm.executeQuery();
				rs.next();
				String bcname = rs.getString("bcname");
				pstm = conn.prepareStatement("SELECT COUNT(*) from copy WHERE callnum = ?");
				pstm.setString(1, callNum);
				rs = pstm.executeQuery();
				rs.next();
				int totalBooks = rs.getInt("COUNT(*)");
				pstm = conn.prepareStatement("SELECT COUNT(*) from borrow WHERE callnum = ? AND returndate = NULL");
				pstm.setString(1, callNum);
				rs = pstm.executeQuery();
				rs.next();
				int borrowedBooks = rs.getInt("COUNT(*)");
				int availableBooks = totalBooks - borrowedBooks;
				System.out.printf("|" + callNum + "|" + title + "|" + bcname + "|");
				for (int i = 0; i < authors.size(); i++) {
					if (i == 0)
						System.out.printf(authors.get(i));
					else
						System.out.printf(", " + authors.get(i));
				}
				if (rating != -1)
					System.out.println("|" + String.valueOf(rating) + "|" + String.valueOf(availableBooks) + "|");
				else
					System.out.println("|null|" + String.valueOf(availableBooks) + "|");
			}
			rs.close();
			pstm.close();
		} catch (Exception e) {
			System.out.println("[Error]: " + e.getMessage());
		}
	}

	// function for user to search for books based on call number, title, or author
	public void SearchBooks() {
		Scanner keyboard = new Scanner(System.in);
		PreparedStatement pstm = null;
		ResultSet rs = null;
		String callNum = "";
		System.out.println("Choose the Search criterion:");
		System.out.println("1. call number");
		System.out.println("2. title");
		System.out.println("3. author");
		System.out.print("Choose the search criterion: ");
		this.option = Integer.parseInt(keyboard.nextLine());
		System.out.print("Type in the Search Keyword: ");
		if (this.option == 1) // search by call number
		{
			try {
				callNum = keyboard.nextLine();
				System.out.println("|Call Num|Title|Book Category|Author|Rating|Available No. of Copy|");
				Search(callNum);
			} catch (Exception e) {
				System.out.println("[Error]: " + e.getMessage());
			}
			System.out.println("End of Query\n");
		} else if (this.option == 2) // partial search by title
		{
			try {
				String title = keyboard.nextLine();
				pstm = conn.prepareStatement("SELECT callnum from book WHERE title LIKE ? ORDER BY callnum");
				pstm.setString(1, "%" + title + "%");
				rs = pstm.executeQuery();
				System.out.println("|Call Num|Title|Book Category|Author|Rating|Available No. of Copy|");
				while (rs.next()) {
					callNum = rs.getString("callnum");
					Search(callNum);
				}
			} catch (Exception e) {
				System.out.println("[Error]: " + e.getMessage());
			}
			System.out.println("End of Query\n");
		} else if (this.option == 3) // partial search by author
		{
			try {
				String author = keyboard.nextLine();
				pstm = conn.prepareStatement("SELECT callnum from authorship WHERE aname LIKE ? ORDER BY callnum");
				pstm.setString(1, "%" + author + "%");
				rs = pstm.executeQuery();
				System.out.println("|Call Num|Title|Book Category|Author|Rating|Available No. of Copy|");
				while (rs.next()) {
					callNum = rs.getString("callnum");
					Search(callNum);
				}
			} catch (Exception e) {
				System.out.println("[Error]: " + e.getMessage());
			}
			System.out.println("End of Query\n");
		} else
			System.out.println("\nIncorrect input\n");
	}

	// function to display the loan records of a user
	public void ShowUserRecords() {
		Scanner keyboard = new Scanner(System.in);
		System.out.print("Enter the User ID: ");
		String userID = keyboard.nextLine();
		PreparedStatement pstm = null;
		PreparedStatement pstm2 = null;
		ResultSet rs = null;
		ResultSet rs2 = null;
		System.out.println("Loan Record:");
		System.out.println("|CallNum|CopyNum|Title|Author|Check-out|Returned?|");
		try {
			String ret = ""; // yes or no value to indicate Returned?
			pstm = conn.prepareStatement(
					"SELECT callnum, copynum, checkout, returndate from borrow WHERE libuid = ? ORDER BY checkout DESC");
			pstm.setString(1, userID);
			rs = pstm.executeQuery();
			while (rs.next()) {
				ArrayList<String> authors = new ArrayList<String>();
				String callNum = rs.getString("callnum");
				String copyNum = rs.getString("copynum");
				Date checkout = rs.getDate("checkout");
				Date returned = rs.getDate("returndate");
				if (rs.wasNull())
					ret = "No";
				else
					ret = "Yes";
				pstm2 = conn.prepareStatement("SELECT title from book WHERE callnum = ?");
				pstm2.setString(1, callNum);
				rs2 = pstm2.executeQuery();
				rs2.next();
				String title = rs2.getString("title");
				pstm2 = conn.prepareStatement("SELECT aname from authorship WHERE callnum = ?");
				pstm2.setString(1, callNum);
				// creating a result set to handle multiple authors
				ResultSet multipleAuthors = pstm2.executeQuery();
				while (multipleAuthors.next()) {
					// if multiple authors exist for a book, all of them are added to the ArrayList
					// 'authors'
					authors.add(multipleAuthors.getString(1));
				}
				System.out.printf("|" + callNum + "|" + copyNum + "|" + title + "|");
				for (int i = 0; i < authors.size(); i++) {
					if (i == 0)
						System.out.printf(authors.get(i));
					else
						System.out.printf(", " + authors.get(i));
				}
				System.out.printf("|" + checkout + "|" + ret + "|");
				System.out.println();
			}
			System.out.println("\nEnd of Query\n");
		} catch (Exception e) {
			System.out.println("[Error]: " + e.getMessage());
		}
	}

	public void ShowUserMenu() {
		System.out.print("\n");
		while (true) {
			System.out.println("-----Operations for library user menu-----");
			System.out.println("What kind of operation would you like to perform?");
			System.out.println("1. Search for Books");
			System.out.println("2. Show loan record of a user");
			System.out.println("3. Return to the main menu");
			System.out.print("Enter your choice: ");
			Scanner keyboard = new Scanner(System.in);
			this.option = Integer.parseInt(keyboard.nextLine());
			switch (this.option) {
			case 1:
				System.out.print("\n");
				SearchBooks();
				break;
			case 2:
				ShowUserRecords();
				break;
			case 3:
				System.out.print("\n");
				return;
			default:
				System.out.println("Invalid Choice! Please try again with a valid choice!\n");
			}
		}
	}

}
