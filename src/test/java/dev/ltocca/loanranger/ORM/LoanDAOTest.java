




package dev.ltocca.loanranger.ORM;

import dev.ltocca.loanranger.domainModel.*;
import dev.ltocca.loanranger.domainModel.State.LoanedState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


class LoanDAOTest extends OrmIntegrationTestBase {

    private Library testLibrary;
    private Book testBook;
    private BookCopy testBookCopy;
    private BookCopy testBookCopy2; 
    private Member testMember;
    private Member testMember2;

    @BeforeEach
    void setUp() throws Exception {
        executeSchemaScript();
        testLibrary = createTestLibrary();
        testBook = createTestBook();
        testBookCopy = createTestBookCopy(testBook, testLibrary);
        testBookCopy2 = createTestBookCopy(testBook, testLibrary); 
        testMember = createTestMember();
        
        testMember2 = new Member();
        testMember2.setUsername("member2");
        testMember2.setName("Member Two");
        testMember2.setEmail("member2@test.com");
        testMember2.setPassword("password");
        testMember2.setRole(UserRole.MEMBER);
        userDAO.createUser(testMember2);
    }

    @Test
    void createLoan_WithLoanObject_ShouldCreateLoanSuccessfully() {
        
        Loan loan = new Loan(testBookCopy, testMember, LocalDate.now());
        
        Loan createdLoan = loanDAO.createLoan(loan);
        
        assertThat(createdLoan).isNotNull();
        assertThat(createdLoan.getId()).isNotNull();
        assertThat(createdLoan.getBookCopy().getCopyId()).isEqualTo(testBookCopy.getCopyId());
        assertThat(createdLoan.getMember().getId()).isEqualTo(testMember.getId());
        assertThat(createdLoan.getLoanDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void createLoan_WithBookCopyAndMember_ShouldCreateLoanSuccessfully() {
        
        Loan createdLoan = loanDAO.createLoan(testBookCopy, testMember);
        
        assertThat(createdLoan).isNotNull();
        assertThat(createdLoan.getId()).isNotNull();
        assertThat(createdLoan.getBookCopy().getCopyId()).isEqualTo(testBookCopy.getCopyId());
        assertThat(createdLoan.getMember().getId()).isEqualTo(testMember.getId());
        assertThat(createdLoan.getLoanDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void getLoanById_ShouldReturnLoanWhenExists() {
        
        Loan loan = createTestLoan(testBookCopy, testMember);
        assertThat(loan.getId()).isNotNull(); 
        
        Optional<Loan> foundLoan = loanDAO.getLoanById(loan.getId());
        
        assertThat(foundLoan).isPresent();
        assertThat(foundLoan.get().getId()).isEqualTo(loan.getId());
        assertThat(foundLoan.get().getBookCopy().getCopyId()).isEqualTo(testBookCopy.getCopyId());
    }

    @Test
    void getLoanById_ShouldReturnEmptyWhenLoanNotFound() {
        
        Optional<Loan> foundLoan = loanDAO.getLoanById(999L);
        
        assertThat(foundLoan).isEmpty();
    }

    @Test
    void getLoanByBookCopy_ShouldReturnLoanWhenExists() {
        
        Loan loan = createTestLoan(testBookCopy, testMember);
        assertThat(loan.getId()).isNotNull();
        
        Optional<Loan> foundLoan = loanDAO.getLoanByBookCopy(testBookCopy);
        
        assertThat(foundLoan).isPresent();
        assertThat(foundLoan.get().getBookCopy().getCopyId()).isEqualTo(testBookCopy.getCopyId());
    }

    @Test
    void getLoanByBookCopyId_ShouldReturnLoanWhenExists() {
        
        Loan loan = createTestLoan(testBookCopy, testMember);
        assertThat(loan.getId()).isNotNull();
        
        Optional<Loan> foundLoan = loanDAO.getLoanByBookCopyId(testBookCopy.getCopyId());
        
        assertThat(foundLoan).isPresent();
        assertThat(foundLoan.get().getBookCopy().getCopyId()).isEqualTo(testBookCopy.getCopyId());
    }

    @Test
    void updateLoan_ShouldUpdateLoanInformation() {
        
        Loan loan = createTestLoan(testBookCopy, testMember);
        assertThat(loan.getId()).isNotNull();
        loan.setReturnDate(LocalDate.now());
        
        loanDAO.updateLoan(loan);
        
        Optional<Loan> updatedLoan = loanDAO.getLoanById(loan.getId());
        assertThat(updatedLoan).isPresent();
        assertThat(updatedLoan.get().getReturnDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void updateDueDate_WithDays_ShouldExtendDueDate() {
        
        Loan loan = createTestLoan(testBookCopy, testMember);
        assertThat(loan.getId()).isNotNull();
        LocalDate originalDueDate = loan.getDueDate();
        
        loanDAO.updateDueDate(loan.getId(), 15);
        
        Optional<Loan> updatedLoan = loanDAO.getLoanById(loan.getId());
        assertThat(updatedLoan).isPresent();
        assertThat(updatedLoan.get().getDueDate()).isEqualTo(originalDueDate.plusDays(15));
    }

    @Test
    void updateDueDate_WithDate_ShouldUpdateDueDate() {
        
        Loan loan = createTestLoan(testBookCopy, testMember);
        assertThat(loan.getId()).isNotNull();
        LocalDate newDueDate = LocalDate.now().plusDays(60);
        
        loanDAO.updateDueDate(loan.getId(), newDueDate);
        
        Optional<Loan> updatedLoan = loanDAO.getLoanById(loan.getId());
        assertThat(updatedLoan).isPresent();
        assertThat(updatedLoan.get().getDueDate()).isEqualTo(newDueDate);
    }

    @Test
    void findLoansByMember_ShouldReturnAllMemberLoans() {
        
        createTestLoan(testBookCopy, testMember);
        
        Loan loan2 = createTestLoan(testBookCopy2, testMember2);
        
        List<Loan> memberLoans = loanDAO.findLoansByMember(testMember);
        
        assertThat(memberLoans).hasSize(1);
        assertThat(memberLoans.get(0).getMember().getId()).isEqualTo(testMember.getId());
    }

    @Test
    void findActiveLoansByMember_ShouldReturnOnlyActiveLoans() {
        
        Loan activeLoan = createTestLoan(testBookCopy, testMember);
        assertThat(activeLoan.getId()).isNotNull();
        
        Loan returnedLoan = createTestLoan(testBookCopy2, testMember);
        assertThat(returnedLoan.getId()).isNotNull();
        returnedLoan.setReturnDate(LocalDate.now());
        loanDAO.updateLoan(returnedLoan);
        
        List<Loan> activeLoans = loanDAO.findActiveLoansByMember(testMember);
        
        assertThat(activeLoans).hasSize(1);
        assertThat(activeLoans.get(0).getId()).isEqualTo(activeLoan.getId());
        assertThat(activeLoans.get(0).getReturnDate()).isNull();
    }

    @Test
    void findOverdueLoans_ShouldReturnOnlyOverdueLoans() {
        
        Loan loan = createTestLoan(testBookCopy, testMember);
        assertThat(loan.getId()).isNotNull();
        
        LocalDate pastDueDate = LocalDate.now().minusDays(1);
        loanDAO.updateDueDate(loan.getId(), pastDueDate);
        
        List<Loan> overdueLoans = loanDAO.findOverdueLoans();
        
        assertThat(overdueLoans).hasSize(1);
        assertThat(overdueLoans.get(0).getId()).isEqualTo(loan.getId());
        assertThat(overdueLoans.get(0).getDueDate()).isEqualTo(pastDueDate);
    }

    @Test
    void findActiveLoansByLibrary_ShouldReturnActiveLoansForLibrary() {
        
        Loan activeLoan = createTestLoan(testBookCopy, testMember);
        assertThat(activeLoan.getId()).isNotNull();
        
        List<Loan> activeLoans = loanDAO.findActiveLoansByLibrary(testLibrary);
        
        assertThat(activeLoans).hasSize(1);
        assertThat(activeLoans.get(0).getBookCopy().getLibrary().getId()).isEqualTo(testLibrary.getId());
    }

    @Test
    void findMemberOverdueLoans_ShouldReturnOverdueLoansForMember() {
        
        Loan loan = createTestLoan(testBookCopy, testMember);
        assertThat(loan.getId()).isNotNull();
        
        LocalDate pastDueDate = LocalDate.now().minusDays(1);
        loanDAO.updateDueDate(loan.getId(), pastDueDate);
        
        List<Loan> overdueLoans = loanDAO.findMemberOverdueLoans(testMember.getId());
        
        assertThat(overdueLoans).hasSize(1);
        assertThat(overdueLoans.get(0).getId()).isEqualTo(loan.getId());
        assertThat(overdueLoans.get(0).getMember().getId()).isEqualTo(testMember.getId());
        assertThat(overdueLoans.get(0).getDueDate()).isEqualTo(pastDueDate);
    }

    @Test
    void deleteLoan_ShouldRemoveLoanFromDatabase() {
        
        Loan loan = createTestLoan(testBookCopy, testMember);
        
        loanDAO.deleteLoan(loan.getId());
        
        Optional<Loan> deletedLoan = loanDAO.getLoanById(loan.getId());
        assertThat(deletedLoan).isEmpty();
    }
}