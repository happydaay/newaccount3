package newaccount.domain;

import java.util.Date;
import java.util.List;
import java.util.*;
import javax.persistence.*;

import org.springframework.beans.BeanUtils;

import lombok.Data;
import newaccount.IncomecheckApplication;
import newaccount.domain.IncomeVerifiedE;
import newaccount.external.ExternalCheck;

@Entity
@Table(name = "IncomeVerificationResultA_table")
@Data
public class IncomeVerificationResultA {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String custNo;

    private String verifyResult;

    private Long incomeAmount;

    private String appliedStatus;

    private String regNo;

    @PostPersist
    public void onPostPersist() {
        IncomeVerifiedE incomeVerifiedE = new IncomeVerifiedE(this);
        incomeVerifiedE.publishAfterCommit();

        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

       
            
    }

    public static IncomeVerificationResultARepository repository() {
        IncomeVerificationResultARepository incomeVerificationResultARepository = IncomecheckApplication.applicationContext.getBean(
            IncomeVerificationResultARepository.class
        );
        return incomeVerificationResultARepository;
    }

    public static void incomeVerify(PreAppliedE preAppliedE) {
        IncomeVerificationResultA incomeVerificationResultA = new IncomeVerificationResultA();
        BeanUtils.copyProperties(preAppliedE, incomeVerificationResultA);

        
        System.out.println("-------incomeVerify(PreAppliedE preAppliedE)-----------------------------------------------------------------") ;

        System.out.println("getRegNo=>: " + incomeVerificationResultA.getRegNo());

        System.out.println("------------------------------------------------------------------------") ;

        // mappings goes here
        newaccount.external.ExternalCheck checkResult = IncomecheckApplication.applicationContext
            .getBean(newaccount.external.ExternalCheckService.class)
            .externalCheck(incomeVerificationResultA.getRegNo());         
                    
        System.out.println("------------------------------------------------------------------------") ;
        System.out.println("???????????? Event -> ???????????? Policy ?????? ");
        System.out.println("------------------------------------------------------------------------") ;
        
        //Random rand = new Random();
        //int iValue = 10000000 * rand.nextInt(10);

        incomeVerificationResultA.setCustNo(preAppliedE.getCustNo()); 

        Long iValue = checkResult.getIncomeExtAmt();  
     
        // ???????????? 5000?????? ???????????? ?????????????????? Y , ???????????? PASSED
        if(iValue > 50000000)
        {
            incomeVerificationResultA.setVerifyResult("Y");
            incomeVerificationResultA.setAppliedStatus("PASSED");
        
        }
        // ???????????? 5000?????? ???????????? ?????????????????? N , ???????????? FAILED
        else
        {
            incomeVerificationResultA.setVerifyResult("N");
            incomeVerificationResultA.setAppliedStatus("FAILED");
        }
        
        // ????????? ?????? 
        repository().save(incomeVerificationResultA);

        System.out.println("---????????????---------------------------------------------------------------------") ;
        System.out.println("???????????? :" + incomeVerificationResultA.getCustNo());
        System.out.println("???????????? :" + incomeVerificationResultA.getRegNo());
        System.out.println("???????????? :" + incomeVerificationResultA.getAppliedStatus());
        System.out.println("???????????? :" + incomeVerificationResultA.getIncomeAmount());
        System.out.println("------------------------------------------------------------------------") ;            
        /** Example 1:  new item 
        IncomeVerificationResultA incomeVerificationResultA = new IncomeVerificationResultA();
        repository().save(incomeVerificationResultA);

        IncomeVerifiedE incomeVerifiedE = new IncomeVerifiedE(incomeVerificationResultA);
        incomeVerifiedE.publishAfterCommit();
        */

        /** Example 2:  finding and process
        
        repository().findById(preAppliedE.get???()).ifPresent(incomeVerificationResultA->{
            
            incomeVerificationResultA // do something
            repository().save(incomeVerificationResultA);

            IncomeVerifiedE incomeVerifiedE = new IncomeVerifiedE(incomeVerificationResultA);
            incomeVerifiedE.publishAfterCommit();

         });
        */

    }
}
