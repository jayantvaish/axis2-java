<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>

      <!--Template for in out message receiver -->
      <xsl:template match="/interface">
        <xsl:variable name="skeletonname"><xsl:value-of select="@name"/></xsl:variable>
        <xsl:variable name="qname"><xsl:value-of select="@qname"/></xsl:variable>
        <xsl:variable name="method-prefix"><xsl:value-of select="@prefix"/></xsl:variable>
        <xsl:variable name="svcop-prefix"><xsl:value-of select="@svcop_prefix"/></xsl:variable>
        <xsl:variable name="svcname"><xsl:value-of select="@svcname"/></xsl:variable>
        <xsl:variable name="soapVersion"><xsl:value-of select="@soap-version"/></xsl:variable>

        /**
         * <xsl:value-of select="@name"/>.c
         *
         * This file was auto-generated from WSDL for "<xsl:value-of select="$qname"/>" service
         * by the Apache Axis2 version: #axisVersion# #today#
         *  <xsl:value-of select="$skeletonname"/>
         */

        #include "<xsl:value-of select="$svcop-prefix"/>.h"
        #include &lt;axis2_svc_skeleton.h&gt;
        #include &lt;stdio.h&gt;
        #include &lt;axis2_svc.h&gt;

        /**
         * functions prototypes
         */

        /* On fault, handle the fault */
        axiom_node_t* AXIS2_CALL
        <xsl:value-of select="$method-prefix"/>_on_fault(axis2_svc_skeleton_t *svc_skeleton,
                  const axutil_env_t *env, axiom_node_t *node);

        /* Free the service */
        int AXIS2_CALL
        <xsl:value-of select="$method-prefix"/>_free(axis2_svc_skeleton_t *svc_skeleton,
                  const axutil_env_t *env);

        /* This method invokes the right service method */
        axiom_node_t* AXIS2_CALL
        <xsl:value-of select="$method-prefix"/>_invoke(axis2_svc_skeleton_t *svc_skeleton,
                    const axutil_env_t *env,
                    axiom_node_t *node,
                    axis2_msg_ctx_t *msg_ctx);

        /* Initializing the environment  */
        int AXIS2_CALL
        <xsl:value-of select="$method-prefix"/>_init(axis2_svc_skeleton_t *svc_skeleton,
                        const axutil_env_t *env);

        /* Create the service  */
        axis2_svc_skeleton_t* AXIS2_CALL
        <xsl:value-of select="$method-prefix"/>_create(const axutil_env_t *env);

        static const axis2_svc_skeleton_ops_t <xsl:value-of select="$skeletonname"/>_svc_skeleton_ops_var = {
            <xsl:value-of select="$method-prefix"/>_init,
            <xsl:value-of select="$method-prefix"/>_invoke,
            <xsl:value-of select="$method-prefix"/>_on_fault,
            <xsl:value-of select="$method-prefix"/>_free
        };


        /**
         * Implementations for the functions
         */

	axis2_svc_skeleton_t* AXIS2_CALL
	<xsl:value-of select="$method-prefix"/>_create(const axutil_env_t *env)
	{
	    axis2_svc_skeleton_t *svc_skeleton = NULL;
        /* Allocate memory for the structs */
        svc_skeleton = (axis2_svc_skeleton_t *)AXIS2_MALLOC(env->allocator,
            sizeof(axis2_svc_skeleton_t));

        svc_skeleton->ops = &amp;<xsl:value-of select="$skeletonname"/>_svc_skeleton_ops_var;


	    return svc_skeleton;
	}


	int AXIS2_CALL
	<xsl:value-of select="$method-prefix"/>_init(axis2_svc_skeleton_t *svc_skeleton,
	                        const axutil_env_t *env)
	{
	    /* Any initialization stuff of <xsl:value-of select="$svcname"/> goes here */
	    return AXIS2_SUCCESS;
	}

	int AXIS2_CALL
	<xsl:value-of select="$method-prefix"/>_free(axis2_svc_skeleton_t *svc_skeleton,
				 const axutil_env_t *env)
	{

        /* Free the service skeleton */
        if (svc_skeleton)
        {
            AXIS2_FREE(env->allocator, svc_skeleton);
            svc_skeleton = NULL;
        }

        return AXIS2_SUCCESS;
	}


	/*
	 * This method invokes the right service method
	 */
	axiom_node_t* AXIS2_CALL
	<xsl:value-of select="$method-prefix"/>_invoke(axis2_svc_skeleton_t *svc_skeleton,
				const axutil_env_t *env,
				axiom_node_t *content_node,
				axis2_msg_ctx_t *msg_ctx)
	{
         /* depending on the function name invoke the
          * corresponding  method
          */

          axis2_op_ctx_t *operation_ctx = NULL;
          axis2_op_t *operation = NULL;
          axutil_qname_t *op_qname = NULL;
          axis2_char_t *op_name = NULL;

          axiom_node_t *ret_node = NULL;

          <xsl:for-each select="method">
            <xsl:text>
            </xsl:text>
            <xsl:variable name="position"><xsl:value-of select="position()"/></xsl:variable>
            <xsl:variable name="outputtype">
              <xsl:choose>
                <xsl:when test="output/param/@ours">adb_<xsl:value-of select="output/param/@type"/>_t*</xsl:when>
                <xsl:when test="not(output/param/@type)">axis2_status_t</xsl:when>
                <xsl:otherwise><xsl:value-of select="output/param/@type"></xsl:value-of></xsl:otherwise>
              </xsl:choose>
            </xsl:variable>
            <xsl:value-of select="$outputtype"/> ret_val<xsl:value-of select="$position"/><xsl:if test="output/param/@ours"> = NULL</xsl:if>;
            <xsl:if test="input/param/@type!=''">
              <xsl:variable name="inputtype">
                <xsl:if test="input/param/@ours">adb_</xsl:if><xsl:value-of select="input/param/@type"/><xsl:if test="input/param/@ours">_t*</xsl:if>
              </xsl:variable>
              <xsl:value-of select="$inputtype"/> input_val<xsl:value-of select="$position"/><xsl:if test="input/param/@ours"> = NULL</xsl:if>;
            </xsl:if>
          </xsl:for-each>

          operation_ctx = axis2_msg_ctx_get_op_ctx(msg_ctx, env);
          operation = axis2_op_ctx_get_op(operation_ctx, env);
          op_qname = (axutil_qname_t *)axis2_op_get_qname(operation, env);
          op_name = axutil_qname_get_localpart(op_qname, env);

          if (op_name)
          {
            <xsl:for-each select="method">
                <xsl:variable name="position"><xsl:value-of select="position()"/></xsl:variable>
                <xsl:variable name="name"><xsl:value-of select="@name"/></xsl:variable>
                <xsl:variable name="method-name"><xsl:value-of select="@name"/></xsl:variable>
                <xsl:variable name="method-ns"><xsl:value-of select="@namespace"/> </xsl:variable>
                <xsl:variable name="outputCapsType"><xsl:value-of select="output/param/@caps-type"/> </xsl:variable>
                <xsl:variable name="outputtype"><xsl:value-of select="output/param/@type"/></xsl:variable>

                if ( axutil_strcmp(op_name, "<xsl:value-of select="@localpart"/>") == 0 )
                {
                    <xsl:if test="input/param/@type!=''">
                    input_val<xsl:value-of select="$position"/> = <xsl:choose>
                        <xsl:when test="input/param/@ours">
                        adb_<xsl:value-of select="input/param/@type"/>_create( env);
                        if( AXIS2_FAILURE == adb_<xsl:value-of select="input/param/@type"/>_deserialize(input_val<xsl:value-of select="$position"/>, env, &amp;content_node, NULL, AXIS2_FALSE))
                        {
                            adb_<xsl:value-of select="input/param/@type"/>_free(input_val<xsl:value-of select="$position"/>, env);
                      
                            AXIS2_ERROR_SET(env->error, AXIS2_ERROR_DATA_ELEMENT_IS_NULL, AXIS2_FAILURE);
                            AXIS2_LOG_ERROR( env->log, AXIS2_LOG_SI, "NULL returnted from the <xsl:value-of select="input/param/@type"/>_deserialize: "
                                        "This should be due to an invalid XML");
                            return NULL;      
                        }
                        </xsl:when>
                        <xsl:otherwise>content_node;</xsl:otherwise>
                        </xsl:choose>
                    </xsl:if>
                    ret_val<xsl:value-of select="$position"/> =  <xsl:value-of select="$svcop-prefix"/>_<xsl:value-of select="$method-name"/>(env <xsl:if test="input/param/@type!=''">,</xsl:if>
                                                <xsl:if test="input/param/@type!=''">input_val<xsl:value-of select="$position"/></xsl:if> );
                    <xsl:choose>
                    <xsl:when test="output/param/@type">
                        if ( NULL == ret_val<xsl:value-of select="$position"/> )
                        {
                            AXIS2_ERROR_SET(env->error, AXIS2_ERROR_DATA_ELEMENT_IS_NULL, AXIS2_FAILURE);
                            AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL returnted from the business logic from <xsl:value-of select="$method-name"/> ");
                            return NULL; 
                        }
                        ret_node = <xsl:choose>
                                       <xsl:when test="output/param/@ours">
                                   adb_<xsl:value-of select="$outputtype"/>_serialize(ret_val<xsl:value-of select="$position"/>, env, NULL, NULL, AXIS2_TRUE, NULL, NULL);
                                   adb_<xsl:value-of select="$outputtype"/>_free(ret_val<xsl:value-of select="$position"/>, env);
                                   <xsl:if test="input/param/@type!=''">
                                    adb_<xsl:value-of select="input/param/@type"/>_free(input_val<xsl:value-of select="$position"/>, env);
                                   </xsl:if>
                                       </xsl:when>
                                       <xsl:otherwise>ret_val<xsl:value-of select="$position"/>;</xsl:otherwise>
                                    </xsl:choose>
                        return ret_node;
                    </xsl:when>
                    <xsl:otherwise>
                        if( AXIS2_FAILURE == ret_val<xsl:value-of select="$position"/> )
                        {
                            AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL returnted from the business logic from <xsl:value-of select="$method-name"/> ");
                        }
                        return NULL;
                    </xsl:otherwise>
                    </xsl:choose>

                    <!-- below was  prior to the databinding -->
                    <!-- <xsl:if test="$outputtype!=''">return </xsl:if>
                    <xsl:text> </xsl:text>
                    <xsl:value-of select="$svcop-prefix"/>_<xsl:value-of select="$method-name"/>(env <xsl:for-each select="input/param[@type!='']"> ,
                                         content_node </xsl:for-each>);
                     <xsl:if test="$outputtype=''">return NULL;</xsl:if> -->

                }
             </xsl:for-each>
             }
          printf("<xsl:value-of select="$skeletonname"/> service ERROR: invalid OM parameters in request\n");
          return NULL;
    }

    axiom_node_t* AXIS2_CALL
    <xsl:value-of select="$method-prefix"/>_on_fault(axis2_svc_skeleton_t *svc_skeleton,
                  const axutil_env_t *env, axiom_node_t *node)
	{
		axiom_node_t *error_node = NULL;
		axiom_element_t *error_ele = NULL;
		error_ele = axiom_element_create(env, node, "fault", NULL,
    					&amp;error_node);
		axiom_element_set_text(error_ele, env, "<xsl:value-of select="$qname"/> failed",
    					error_node);
		return error_node;
	}


	/**
	 * Following block distinguish the exposed part of the dll.
 	 */

    AXIS2_EXTERN int
    axis2_get_instance(struct axis2_svc_skeleton **inst,
	                        const axutil_env_t *env)
	{
		*inst = <xsl:value-of select="$method-prefix"/>_create(env);

        if(!(*inst))
        {
            return AXIS2_FAILURE;
        }

  		return AXIS2_SUCCESS;
	}

	AXIS2_EXTERN int 
    axis2_remove_instance(axis2_svc_skeleton_t *inst,
                            const axutil_env_t *env)
	{
        axis2_status_t status = AXIS2_FAILURE;
       	if (inst)
        {
            status = AXIS2_SVC_SKELETON_FREE(inst, env);
        }
    	return status;
	}


    </xsl:template>

</xsl:stylesheet>