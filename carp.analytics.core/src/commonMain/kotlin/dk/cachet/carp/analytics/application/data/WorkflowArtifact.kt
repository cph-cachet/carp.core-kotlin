package dk.cachet.carp.analytics.application.data

import dk.cachet.carp.common.application.data.Data
import kotlin.js.JsExport


/**
 * [Data] produced by a step in a DSP workflow.
 */
@JsExport
interface WorkflowArtifact : Data


/**
 * A time series or value-shaped result of an analysis.
 */
@JsExport
interface AnalyticResult : WorkflowArtifact


/**
 * A reference to a rendered file: an image, a plot, a document etc.
 *
 * Media artefacts never carry their payload. The bytes live in file or object storage; the artefact
 * holds metadata and a pointer to them.
 */
@JsExport
interface MediaArtifact : WorkflowArtifact


/**
 * A record of an action a workflow asked the platform to take.
 *
 * The action itself is a service call; a [ControlSignal] is the auditable trace it leaves behind.
 */
@JsExport
interface ControlSignal : WorkflowArtifact
