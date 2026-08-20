/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data.io

import org.cr.pipeline.data.JobApplicationRepository

actual fun createDataPortController(repository: JobApplicationRepository): DataPortController = UnsupportedDataPortController
