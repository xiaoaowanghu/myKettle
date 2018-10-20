/*!
 * HITACHI VANTARA PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2018 Hitachi Vantara. All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Hitachi Vantara and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Hitachi Vantara and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Hitachi Vantara is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Hitachi Vantara,
 * explicitly covering such access.
 */
package org.pentaho.di.trans;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.spy;

/**
 * Created by Yury_Bakhmutski on 2/8/2017.
 */
@RunWith( PowerMockRunner.class )
public class StepWithMappingMetaTest {

  @Mock
  TransMeta transMeta;

  @Test
  public void loadMappingMeta() throws Exception {
    String variablePath = "Internal.Entry.Current.Directory";
    String virtualDir = "/testFolder/CDA-91";
    String fileName = "testTrans.ktr";

    VariableSpace variables = new Variables();
    StepMeta stepMeta = new StepMeta();
    TransMeta parentTransMeta = new TransMeta();
    stepMeta.setParentTransMeta( parentTransMeta );

    RepositoryDirectoryInterface repositoryDirectory = Mockito.mock( RepositoryDirectoryInterface.class );
    when( repositoryDirectory.toString() ).thenReturn( virtualDir );
    stepMeta.getParentTransMeta().setRepositoryDirectory( repositoryDirectory );

    StepWithMappingMeta mappingMetaMock = mock( StepWithMappingMeta.class );
    when( mappingMetaMock.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.FILENAME );
    when( mappingMetaMock.getFileName() ).thenReturn( "${" + variablePath + "}/" + fileName );
    when( mappingMetaMock.getParentStepMeta() ).thenReturn( stepMeta );

    // mock repo and answers
    Repository rep = mock( Repository.class );

    Mockito.doAnswer( new Answer<TransMeta>() {
      @Override
      public TransMeta answer( final InvocationOnMock invocation ) throws Throwable {
        final String originalArgument = (String) ( invocation.getArguments() )[ 0 ];
        // be sure that the variable was replaced by real path
        assertEquals( virtualDir, originalArgument );
        return null;
      }
    } ).when( rep ).findDirectory( anyString() );

    Mockito.doAnswer( new Answer<TransMeta>() {
      @Override
      public TransMeta answer( final InvocationOnMock invocation ) throws Throwable {
        final String originalArgument = (String) ( invocation.getArguments() )[ 0 ];
        // be sure that transformation name was resolved correctly
        assertEquals( fileName, originalArgument );
        return mock( TransMeta.class );
      }
    } ).when( rep ).loadTransformation( anyString(), any( RepositoryDirectoryInterface.class ),
      any( ProgressMonitorListener.class ), anyBoolean(), anyString() );

    StepWithMappingMeta.loadMappingMeta( mappingMetaMock, rep, null, variables, true );
  }

  @Test
  @PrepareForTest( StepWithMappingMeta.class )
  public void testExportResources() throws Exception {
    StepWithMappingMeta stepWithMappingMeta = spy( StepWithMappingMeta.class );
    String testName = "test";
    PowerMockito.mockStatic( StepWithMappingMeta.class );
    when( StepWithMappingMeta.loadMappingMeta( any(), any(), any(), any() ) ).thenReturn( transMeta );
    when( transMeta.exportResources( any(), anyMap(), any(), any(), any() ) ).thenReturn( testName );

    stepWithMappingMeta.exportResources( null, null, null, null, null );
    verify( transMeta ).setFilename( "${" + Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY + "}/" + testName );
    verify( stepWithMappingMeta ).setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );

  }

  @Test
  @PrepareForTest( StepWithMappingMeta.class )
  public void loadMappingMetaTest() throws Exception {

    String childParam = "childParam";
    String childValue = "childValue";
    String paramOverwrite = "paramOverwrite";
    String parentParam = "parentParam";
    String parentValue = "parentValue";

    String variablePath = "Internal.Entry.Current.Directory";
    String virtualDir = "/testFolder/CDA-91";
    String fileName = "testTrans.ktr";

    VariableSpace variables = new Variables();
    variables.setVariable( parentParam, parentValue );
    variables.setVariable( paramOverwrite, parentValue );

    StepMeta stepMeta = new StepMeta();
    TransMeta parentTransMeta = new TransMeta();
    stepMeta.setParentTransMeta( parentTransMeta );

    RepositoryDirectoryInterface repositoryDirectory = Mockito.mock( RepositoryDirectoryInterface.class );
    when( repositoryDirectory.toString() ).thenReturn( virtualDir );
    stepMeta.getParentTransMeta().setRepositoryDirectory( repositoryDirectory );

    StepWithMappingMeta mappingMetaMock = mock( StepWithMappingMeta.class );
    when( mappingMetaMock.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.FILENAME );
    when( mappingMetaMock.getFileName() ).thenReturn( "${" + variablePath + "}/" + fileName );
    when( mappingMetaMock.getParentStepMeta() ).thenReturn( stepMeta );


    Repository rep = mock( Repository.class );
    Mockito.doReturn( Mockito.mock( RepositoryDirectoryInterface.class ) ).when( rep ).findDirectory( anyString() );

    TransMeta child = new TransMeta();
    child.setVariable( childParam, childValue );
    child.setVariable( paramOverwrite, childValue );
    Mockito.doReturn( child ).when( rep ).loadTransformation( anyString(), any(), any(), anyBoolean(), any() );

    TransMeta transMeta = StepWithMappingMeta.loadMappingMeta( mappingMetaMock, rep, null, variables, true );

    Assert.assertNotNull( transMeta );

    //When the child parameter does exist in the parent parameters, overwrite the child parameter by the parent parameter.
    Assert.assertEquals( parentValue, transMeta.getVariable( paramOverwrite ) );

    //When the child parameter does not exist in the parent parameters, keep it.
    Assert.assertEquals( childValue, transMeta.getVariable( childParam ) );

    //All other parent parameters need to get copied into the child parameters  (when the 'Inherit all
    //variables from the transformation?' option is checked)
    Assert.assertEquals( parentValue, transMeta.getVariable( parentParam ) );
  }

  @Test
  @PrepareForTest( StepWithMappingMeta.class )
  public void activateParamsTest() throws Exception {
    String childParam = "childParam";
    String childValue = "childValue";
    String paramOverwrite = "paramOverwrite";
    String parentValue = "parentValue";

    VariableSpace parent = new Variables();
    parent.setVariable( paramOverwrite, parentValue );

    TransMeta childVariableSpace = new TransMeta();
    childVariableSpace.setParameterValue( childParam, childValue );

    String[] parameters = childVariableSpace.listParameters();
    StepWithMappingMeta.activateParams( childVariableSpace, childVariableSpace, parent,
      parameters, new String[] { childParam, paramOverwrite }, new String[] { childValue, childValue } );

    Assert.assertEquals( childValue, childVariableSpace.getVariable( childParam ) );
    Assert.assertEquals( parentValue, childVariableSpace.getVariable( paramOverwrite ) );
  }
}
