package com.perforce.team.tests.model.p4java;

import com.perforce.p4java.server.AuthTicket;
import com.perforce.p4java.server.AuthTicketsHelper;
import com.perforce.p4java.server.IServer;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.tests.PerforceTestsPlugin;
import com.perforce.team.tests.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;

/**
 * Tickets test
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class TicketsTest extends ConnectionBasedTestCase {

    /**
     * Test tickets lookup
     */
    public void testValid() {
        try {
            URL fileUrl = PerforceTestsPlugin.getDefault().getBundle()
                    .getEntry("/resources/.p4tickets");
            fileUrl = FileLocator.toFileURL(fileUrl);
            String server = "dhcp-140.dhcp.perforce.com:1665";
            AuthTicket ticket = AuthTicketsHelper.getTicket(server,
                    fileUrl.getFile());
            assertNotNull(ticket);
            assertEquals(server, ticket.getServerAddress());
            assertEquals("p4jtestsuper2", ticket.getUserName());
            assertEquals("5C67283BF680A76FB6C29157C64AF0E6",
                    ticket.getTicketValue());
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        }
    }

    /**
     * Test removing an entry in the ticket file
     */
    public void testClear() {
        try {
            URL fileUrl = PerforceTestsPlugin.getDefault().getBundle()
                    .getEntry("/resources/.p4tickets");
            fileUrl = FileLocator.toFileURL(fileUrl);

            File tempTickets = File.createTempFile("test_tickets", ".tmp");
            assertNotNull(tempTickets);

            File current = new File(fileUrl.getFile());

            P4CoreUtils.copyFile(current, tempTickets);

            String content = Utils.getContent(new FileInputStream(current));
            String temp = Utils.getContent(new FileInputStream(tempTickets));
            assertEquals(content, temp);

            AuthTicket[] currTix = AuthTicketsHelper.getTickets(tempTickets);
            assertNotNull(currTix);
            assertTrue(currTix.length > 0);

            AuthTicket ticket = currTix[currTix.length / 2];

            AuthTicket cleared = new AuthTicket();
            cleared.setServerAddress(ticket.getServerAddress());
            cleared.setUserName(ticket.getUserName());
            cleared.setTicketValue(null);

            AuthTicketsHelper.saveTicket(cleared, tempTickets);

            String updated = Utils.getContent(new FileInputStream(tempTickets));
            assertNotNull(updated);
            assertFalse(temp.equals(updated));

            AuthTicket[] newTix = AuthTicketsHelper.getTickets(tempTickets);
            assertNotNull(newTix);
            assertEquals(currTix.length - 1, newTix.length);

            List<AuthTicket> currList = Arrays.asList(currTix);
            for (int i = 0; i < newTix.length; i++) {
                AuthTicket actual = newTix[i];
                int index = currList.indexOf(actual);
                assertTrue(index > -1);
                AuthTicket expected = currList.get(index);
                assertNotNull(expected);
                assertEquals(expected.getServerAddress(),
                        actual.getServerAddress());
                assertEquals(expected.getUserName(), actual.getUserName());
                assertEquals(expected.getTicketValue(), actual.getTicketValue());
                assertEquals(expected, actual);
            }

            List<AuthTicket> newList = Arrays.asList(newTix);
            assertFalse(newList.contains(ticket));

        } catch (Exception e) {
            assertFalse("Exception thrown", true);
        }
    }

    /**
     * Test updating an entry in the ticket file
     */
    public void testUpdate() {

        try {
            URL fileUrl = PerforceTestsPlugin.getDefault().getBundle()
                    .getEntry("/resources/.p4tickets");
            fileUrl = FileLocator.toFileURL(fileUrl);

            File tempTickets = File.createTempFile("test_tickets", ".tmp");
            assertNotNull(tempTickets);

            File current = new File(fileUrl.getFile());

            P4CoreUtils.copyFile(current, tempTickets);

            String content = Utils.getContent(new FileInputStream(current));
            String temp = Utils.getContent(new FileInputStream(tempTickets));
            assertEquals(content, temp);

            AuthTicket[] currTix = AuthTicketsHelper.getTickets(tempTickets);
            assertNotNull(currTix);
            assertTrue(currTix.length > 0);

            AuthTicket ticket = currTix[currTix.length / 2];
            String newValue = "AAABBBCCCDDD";
            ticket.setTicketValue(newValue);

            AuthTicketsHelper.saveTicket(ticket, tempTickets);

            String updated = Utils.getContent(new FileInputStream(tempTickets));
            assertNotNull(updated);
            assertFalse(temp.equals(updated));

            AuthTicket[] newTix = AuthTicketsHelper.getTickets(tempTickets);
            assertNotNull(newTix);
            assertEquals(currTix.length, newTix.length);

            for (int i = 0; i < newTix.length; i++) {
                AuthTicket expected = currTix[i];
                AuthTicket actual = newTix[i];
                assertEquals(expected.getServerAddress(),
                        actual.getServerAddress());
                assertEquals(expected.getUserName(), actual.getUserName());
                assertEquals(expected.getTicketValue(), actual.getTicketValue());
                assertEquals(expected, actual);
            }

        } catch (Exception e) {
            assertFalse("Exception thrown", true);
        }
    }

    /**
     * Test adding an entry in the ticket file
     */
    public void testAdd() {
        try {
            URL fileUrl = PerforceTestsPlugin.getDefault().getBundle()
                    .getEntry("/resources/.p4tickets");
            fileUrl = FileLocator.toFileURL(fileUrl);

            File tempTickets = File.createTempFile("test_tickets", ".tmp");
            assertNotNull(tempTickets);

            File current = new File(fileUrl.getFile());

            P4CoreUtils.copyFile(current, tempTickets);

            String content = Utils.getContent(new FileInputStream(current));
            String temp = Utils.getContent(new FileInputStream(tempTickets));
            assertEquals(content, temp);

            AuthTicket[] currTix = AuthTicketsHelper.getTickets(tempTickets);
            assertNotNull(currTix);
            assertTrue(currTix.length > 0);

            AuthTicket ticket = new AuthTicket("server:"
                    + System.currentTimeMillis(), "test_user",
                    "ABCDEFGHIJKLMNOP");
            AuthTicketsHelper.saveTicket(ticket, tempTickets);

            String updated = Utils.getContent(new FileInputStream(tempTickets));
            assertNotNull(updated);
            assertFalse(temp.equals(updated));

            AuthTicket[] newTix = AuthTicketsHelper.getTickets(tempTickets);
            assertNotNull(newTix);
            assertEquals(currTix.length + 1, newTix.length);

            AuthTicket[] joined = new AuthTicket[currTix.length + 1];
            System.arraycopy(currTix, 0, joined, 0, currTix.length);
            joined[joined.length - 1] = ticket;

            for (int i = 0; i < joined.length; i++) {
                AuthTicket expected = joined[i];
                AuthTicket actual = newTix[i];
                assertEquals(expected.getServerAddress(),
                        actual.getServerAddress());
                assertEquals(expected.getUserName(), actual.getUserName());
                assertEquals(expected.getTicketValue(), actual.getTicketValue());
                assertEquals(expected, actual);
            }

        } catch (Exception e) {
            assertFalse("Exception thrown", true);
        }
    }

    /**
     * Test tickets lookup
     */
    public void testValid2() {
        try {
            URL fileUrl = PerforceTestsPlugin.getDefault().getBundle()
                    .getEntry("/resources/.p4tickets");
            fileUrl = FileLocator.toFileURL(fileUrl);
            String server = "dhcp-140.dhcp.perforce.com:1665";
            String user = "p4jtestsuper";
            AuthTicket ticket = AuthTicketsHelper.getTicket(user, server,
                    fileUrl.getFile());
            assertNotNull(ticket);
            assertEquals(server, ticket.getServerAddress());
            assertEquals(user, ticket.getUserName());
            assertEquals("1CFC5A2FD265C8857BFA1DEE30838CD4",
                    ticket.getTicketValue());
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        }
    }

    /**
     * Test invalid file
     */
    public void testInvalidFile() {
        try {
            String server = "dhcp-140.dhcp.perforce.com:1665";
            String user = "p4jtestsuper";
            AuthTicketsHelper.getTicket(user, server,
                    "/bad/path/" + System.currentTimeMillis() + ".txt");
            assertTrue("Exception not thrown", false);
        } catch (IOException e) {
            assertNotNull("IO exception thrown", e);
        }
    }

    /**
     * Test empty user
     */
    public void testEmptyUser() {
        try {
            URL fileUrl = PerforceTestsPlugin.getDefault().getBundle()
                    .getEntry("/resources/.p4tickets");
            fileUrl = FileLocator.toFileURL(fileUrl);
            String server = "dhcp-140.dhcp.perforce.com:1665";
            AuthTicket ticket = AuthTicketsHelper.getTicket(null, server,
                    fileUrl.getFile());
            assertNotNull(ticket);
            assertEquals(server, ticket.getServerAddress());
            assertEquals("p4jtestsuper2", ticket.getUserName());
            assertEquals("5C67283BF680A76FB6C29157C64AF0E6",
                    ticket.getTicketValue());
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        }
    }

    /**
     * Test ticket saving from login
     */
    public void testLogin() {
        IP4Connection connection = createConnection(true);

        IServer server = connection.getServer();
        assertNotNull(server);

        String current = server.getAuthTicket();
        assertNotNull(current);

        String home = System.getProperty("user.home");
        assertNotNull(home);
        String ticketsFile = home + "/.p4tickets";

        AuthTicket currentTicket = null;
        try {
            currentTicket = AuthTicketsHelper.getTicket(
                    connection.getAddress(), ticketsFile);
        } catch (IOException e1) {
        }
        try {
            connection.logout();
            assertNull(server.getAuthTicket());

            try {
                AuthTicket ticket = AuthTicketsHelper.getTicket(
                        connection.getAddress(), ticketsFile);
                assertNull(ticket);
                connection.login(parameters.getPassword());
                ticket = AuthTicketsHelper.getTicket(connection.getAddress(),
                        ticketsFile);
                assertNotNull(ticket);
                assertEquals(current, ticket.getTicketValue());
            } catch (IOException e) {
                handle("IO exception thrown", e);
            }
        } finally {
            try {
                AuthTicketsHelper.saveTicket(currentTicket, ticketsFile);
            } catch (IOException e) {
                handle("Restoring ticket failed", e);
            }
        }
    }

    /**
     * Test ticket clearing from logout
     */
    public void testLogout() {
        IP4Connection connection = createConnection(true);

        IServer server = connection.getServer();
        assertNotNull(server);

        String current = server.getAuthTicket();
        assertNotNull(current);

        String home = System.getProperty("user.home");
        assertNotNull(home);
        String ticketsFile = home + "/.p4tickets";
        AuthTicket currentTicket = null;
        try {
            AuthTicket ticket = AuthTicketsHelper.getTicket(
                    connection.getAddress(), ticketsFile);
            assertNotNull(ticket);
            currentTicket = ticket;
        } catch (IOException e) {
            handle("IO exception thrown", e);
        }

        try {
            connection.logout();
            assertNull(server.getAuthTicket());

            try {
                AuthTicket ticket = AuthTicketsHelper.getTicket(
                        connection.getAddress(), ticketsFile);
                assertNull(ticket);
            } catch (IOException e) {
                handle("IO exception thrown", e);
            }
        } finally {
            try {
                AuthTicketsHelper.saveTicket(currentTicket, ticketsFile);
            } catch (IOException e) {
                handle("Restoring ticket failed", e);
            }
        }
    }

}
