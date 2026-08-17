package org.cr.pipeline.data.io

import org.cr.pipeline.data.JobApplicationRepository

actual fun createDataPortController(repository: JobApplicationRepository): DataPortController = UnsupportedDataPortController
